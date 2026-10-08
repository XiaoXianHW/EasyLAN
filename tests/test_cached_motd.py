"""Execute the complete production 1.16 rule applier with bounded Java doubles.

The double intentionally separates the MOTD field and cached status description,
matching both 1.16.4 and 1.16.5 MinecraftServer bytecode. It does not open sockets,
launch Minecraft, or claim receiver UI/HTTP acceptance.
"""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]
APPLIER = ROOT / "shared/fabric-group-16-modern/src/main/java/org/xiaoxian/lan/ServerRuleApplier.java"

DOUBLES = {
    "org/xiaoxian/EasyLAN.java": r'''
package org.xiaoxian;
public final class EasyLAN {
    public static boolean allowPVP = true, onlineMode = false, spawnAnimals = false;
    public static boolean spawnNPCs = false, allowFlight = true;
    public static String motd;
}
''',
    "net/minecraft/network/chat/Component.java": r'''
package net.minecraft.network.chat;
public interface Component { String getString(); }
''',
    "net/minecraft/network/chat/TextComponent.java": r'''
package net.minecraft.network.chat;
public final class TextComponent implements Component {
    private final String text;
    public TextComponent(String text) { this.text = text; }
    public String getString() { return text; }
}
''',
    "net/minecraft/network/protocol/status/ServerStatus.java": r'''
package net.minecraft.network.protocol.status;
import net.minecraft.network.chat.Component;
public final class ServerStatus {
    private Component description;
    public final Object players = new Object(), version = new Object(), favicon = new Object();
    public Component getDescription() { return description; }
    public void setDescription(Component value) { description = value; }
}
''',
    "net/minecraft/world/level/GameRules.java": r'''
package net.minecraft.world.level;
import net.minecraft.server.MinecraftServer;
public final class GameRules {
    public static final Object RULE_DOMOBSPAWNING = new Object();
    public final BooleanValue mobSpawning = new BooleanValue();
    public BooleanValue getRule(Object key) { return mobSpawning; }
    public static final class BooleanValue {
        public boolean value = true;
        public void set(boolean value, MinecraftServer server) { this.value = value; }
    }
}
''',
    "net/minecraft/server/MinecraftServer.java": r'''
package net.minecraft.server;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.world.level.GameRules;
public final class MinecraftServer {
    private String motd;
    private final ServerStatus status = new ServerStatus();
    public GameRules rules = new GameRules();
    public boolean pvp, authentication = true, flight, spawnNpcs = true;
    public int port = 25599, maxPlayers = 12, onlinePlayers = 1;
    public MinecraftServer(String initialMotd) {
        motd = initialMotd;
        // Vanilla runServer snapshots the field before Fabric 1.16 SERVER_STARTED.
        status.setDescription(new TextComponent(initialMotd));
    }
    // Deliberately DO NOT synchronize status here: this is the original defect.
    public void setMotd(String value) { motd = value; }
    public String getMotd() { return motd; }
    public ServerStatus getStatus() { return status; }
    public GameRules getGameRules() { return rules; }
    public void setPvpAllowed(boolean value) { pvp = value; }
    public void setUsesAuthentication(boolean value) { authentication = value; }
    public void setFlightAllowed(boolean value) { flight = value; }
    public void setSpawnNPCs(boolean value) { spawnNpcs = value; }
}
''',
    "Harness.java": r'''
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.lan.ServerRuleApplier;

public final class Harness {
    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void verify(MinecraftServer server, String configured, String expected) {
        ServerStatus status = server.getStatus();
        Object players = status.players, version = status.version, favicon = status.favicon;
        EasyLAN.motd = configured;
        ServerRuleApplier.apply(server);
        require(expected.equals(server.getMotd()), "MOTD field was not updated");
        require(expected.equals(server.getStatus().getDescription().getString()),
                "cached status description was not updated");
        require(server.getStatus() == status, "status object was replaced");
        require(status.players == players && status.version == version && status.favicon == favicon,
                "unrelated status fields changed");
        require(server.port == 25599 && server.maxPlayers == 12 && server.onlinePlayers == 1,
                "LAN port or player state changed");
        require(server.pvp && !server.authentication && server.flight && !server.spawnNpcs,
                "existing server rules changed");
        require(server.rules == null || !server.rules.mobSpawning.value,
                "existing world rule changed");
        require(configured == EasyLAN.motd, "application rewrote saved configuration");
    }
    public static void main(String[] args) {
        String scenario = args[0];
        MinecraftServer server = new MinecraftServer("Player - Vanilla World");
        if (scenario.equals("startup")) {
            // This proves the fixture itself reproduces field/cache divergence.
            server.setMotd("field-only change");
            require("Player - Vanilla World".equals(server.getStatus().getDescription().getString()),
                    "fixture incorrectly synchronizes the vanilla setter");
            verify(server, "FABRIC-1.16-CUSTOM", "FABRIC-1.16-CUSTOM");
        } else if (scenario.equals("text")) {
            for (String value : new String[] {"", "  ", "§aEasyLAN 世界\nSecond line", "literal {\"text\":\"hello\"}"}) {
                verify(server, value, value);
            }
            verify(server, null, "");
            verify(server, "after null", "after null");
        } else if (scenario.equals("reapply")) {
            verify(server, "before publication", "before publication");
            server.getStatus().setDescription(new TextComponent("stale cached description"));
            verify(server, "after publication", "after publication");
            verify(server, "after publication", "after publication");
            server.rules = null;
            verify(server, "no world rule object", "no world rule object");
        } else if (scenario.equals("reload")) {
            verify(server, "first world", "first world");
            MinecraftServer reloaded = new MinecraftServer("Another Vanilla World");
            verify(reloaded, "second world", "second world");
            require("first world".equals(server.getStatus().getDescription().getString()),
                    "new server changed old server status");
            verify(server, "first world changed", "first world changed");
            require("second world".equals(reloaded.getStatus().getDescription().getString()),
                    "old server changed new server status");
        } else {
            throw new AssertionError("Unknown scenario: " + scenario);
        }
        System.out.println("PASS " + scenario);
    }
}
''',
}


class CachedMotdRegression(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory()
        cls.addClassCleanup(cls.temporary.cleanup)
        cls.root = Path(cls.temporary.name)
        home = Path(os.environ["JAVA_HOME"]) / "bin" if os.environ.get("JAVA_HOME") else None
        cls.java = str(home / "java") if home else "java"
        cls.javac = str(home / "javac") if home else "javac"
        production = APPLIER.read_text()
        cache_update = "server.getStatus().setDescription(new TextComponent(server.getMotd()));"
        field_update = 'server.setMotd(motd == null ? "" : motd);'
        if production.count(cache_update) != 1 or production.count(field_update) != 1:
            raise AssertionError("Update the negative controls when the production implementation changes")
        for label, source in {
            "production": production,
            "stale_cache": production.replace(cache_update, ""),
            "stale_field": production.replace(field_update, ""),
        }.items():
            directory = cls.root / label
            for name, contents in dict(DOUBLES, **{"org/xiaoxian/lan/ServerRuleApplier.java": source}).items():
                path = directory / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(contents)
            compiled = subprocess.run(
                [cls.javac, "-J-Xmx96m", "-J-XX:ActiveProcessorCount=1", "--release", "8",
                 "-encoding", "UTF-8", "-d", str(directory), *map(str, directory.rglob("*.java"))],
                text=True, capture_output=True, timeout=30)
            if compiled.returncode:
                raise AssertionError(compiled.stdout + compiled.stderr)

    def run_harness(self, variant, scenario):
        return subprocess.run(
            [self.java, "-Xmx64m", "-XX:ActiveProcessorCount=1", "-cp", str(self.root / variant),
             "Harness", scenario], text=True, capture_output=True, timeout=15)

    def assert_scenario(self, scenario):
        result = self.run_harness("production", scenario)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("PASS " + scenario, result.stdout)

    def test_started_application_updates_the_cached_description(self):
        self.assert_scenario("startup")

    def test_empty_null_whitespace_and_literal_text(self):
        self.assert_scenario("text")

    def test_reapplication_repairs_stale_cache_without_changing_other_state(self):
        self.assert_scenario("reapply")

    def test_world_reload_and_independent_server_instances(self):
        self.assert_scenario("reload")

    def test_negative_control_field_only_setter_leaves_cached_description_stale(self):
        result = self.run_harness("stale_cache", "startup")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("cached status description was not updated", result.stderr)

    def test_negative_control_status_update_alone_leaves_motd_field_stale(self):
        result = self.run_harness("stale_field", "startup")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("MOTD field was not updated", result.stderr)


if __name__ == "__main__":
    unittest.main()
