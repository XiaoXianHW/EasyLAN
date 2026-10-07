"""Execute unchanged production methods against deterministic Java doubles.

These checks cover client-thread dispatch, local-first LAN output and configured
player counts. They do not replace packaged Minecraft client acceptance.
"""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
TARGETS = ("1.19.4", "1.20.1", "1.20.6", "1.21.1", "1.21.5", "1.21.11")

CHAT_HARNESS = r'''
import java.util.*;
import java.util.regex.Pattern;
public class ChatThreadHarness {
    static class Component {
        final String value;
        Component(String value) { this.value = value; }
        static Component nullToEmpty(String value) { return new Component(value == null ? "" : value); }
    }
    static class Chat {
        List<String> messages = new ArrayList<>();
        void addMessage(Component component) {
            if (Thread.currentThread() != Minecraft.CLIENT_THREAD)
                throw new AssertionError("Chat mutation outside client thread");
            messages.add(component.value);
        }
    }
    static class Gui {
        Chat chat = new Chat();
        Chat getChat() { return chat; }
    }
    static class Minecraft {
        static final Thread CLIENT_THREAD = Thread.currentThread();
        static Minecraft instance = new Minecraft();
        Gui gui = new Gui();
        Queue<Runnable> pending = new java.util.concurrent.ConcurrentLinkedQueue<>();
        static Minecraft getInstance() { return instance; }
        void execute(Runnable task) { pending.add(task); }
        void drain() { while (!pending.isEmpty()) pending.remove().run(); }
    }
    PRODUCTION_CLASS
    public static void main(String[] args) throws Exception {
        Minecraft minecraft = Minecraft.getInstance();
        List<Throwable> failures = new ArrayList<>();
        Thread worker = new Thread(() -> {
            try {
                ChatUtil.sendMsg("&aLAN ready");
                ChatUtil.sendComponentMsg(new Component("second"));
            } catch (Throwable failure) { failures.add(failure); }
        }, "EasyLAN-worker");
        worker.start();
        worker.join();
        if (!failures.isEmpty()) throw new AssertionError("Worker mutated chat directly", failures.get(0));
        if (!minecraft.gui.chat.messages.isEmpty()) throw new AssertionError("Chat ran before client queue");
        minecraft.drain();
        if (!minecraft.gui.chat.messages.equals(List.of("\u00a7aLAN ready", "second")))
            throw new AssertionError("Formatting or message ordering changed: " + minecraft.gui.chat.messages);
        // A queued message must tolerate GUI teardown before it reaches the client.
        ChatUtil.sendMsg("after shutdown");
        minecraft.gui = null;
        minecraft.drain();
        Minecraft.instance = null;
        ChatUtil.sendMsg("no client");
        System.out.println("PASS: worker chat uses client queue, preserves order and survives teardown");
    }
}
'''

LAN_HARNESS = r'''
import java.util.*;
public class LanInfoHarness {
    static boolean HttpAPI, onlineMode = false;
    static final List<String> messages = new ArrayList<>();
    static class IntegratedServer { int getMaxPlayers() { return 8; } }
    static class GuiShareToLanEdit { static String PortText = "25569"; }
    static class I18n { static String get(String key) { return key; } }
    static class ChatUtil { static void sendMsg(String value) { messages.add(value); } }
    static class PublicLookupFailure extends RuntimeException {}
    static class NetworkUtil {
        static String getLocalIpv4() { return "192.168.1.2"; }
        static String getLocalIpv6() { return "::1"; }
        static String getPublicIPv4() {
            String output = String.join("\n", messages);
            for (String value : List.of("Successfully", "25569", "maxplayer: &a37", "onlineMode: &afalse"))
                if (!output.contains(value)) throw new AssertionError("Local status missing before network: " + value);
            if (HttpAPI && (!output.contains("/status") || !output.contains("/playerlist")))
                throw new AssertionError("API details delayed behind network lookup");
            throw new PublicLookupFailure();
        }
        static boolean checkIpIsPublic() { throw new AssertionError("Failure must end lookup"); }
    }
    static class Executor { void submit(Runnable task) { task.run(); } }
    static class RuntimeState {
        Executor openExecutorService(int count) { return new Executor(); }
        String getHttpApiPort() { return "28960"; }
    }
    static class EasyLAN { static RuntimeState getRuntimeState() { return new RuntimeState(); } }
    static String getLanPort(IntegratedServer server) { return "25569"; }
    static class VersionBridgeResolver {
        static int configuredMaxPlayers = 37;
        static VersionBridgeResolver get() { return new VersionBridgeResolver(); }
        int resolveMaxPlayers(IntegratedServer server) { return configuredMaxPlayers; }
    }
    static boolean isBlank(String value) { return value == null || value.isEmpty(); }
    static String safeValue(Object value) { return value == null ? "" : value.toString(); }
    PRODUCTION_METHOD
    PRODUCTION_RESOLVER
    public static void main(String[] args) {
        for (boolean http : new boolean[] {true, false}) {
            HttpAPI = http;
            messages.clear();
            try { new LanInfoHarness().sendLanInfo(new IntegratedServer()); }
            catch (PublicLookupFailure expected) { }
            String output = String.join("\n", messages);
            if (!http && output.contains("Http-Api")) throw new AssertionError("Disabled API advertised");
            if (output.contains("maxplayer: &a8")) throw new AssertionError("Chat used stale vanilla player count");
        }
        for (int unavailable : new int[] {0, -1}) {
            VersionBridgeResolver.configuredMaxPlayers = unavailable;
            if (resolveMaxPlayers(new IntegratedServer()) != 8)
                throw new AssertionError("Unknown bridge limit must fall back to server limit");
        }
        System.out.println("PASS: configured count and local output survive public lookup failure, HTTP on/off");
    }
}
'''


class LanChatRuntime(unittest.TestCase):
    def run_java(self, name, source):
        java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME") else "java"
        with tempfile.TemporaryDirectory(prefix="easylan-chat-regression-") as tmp:
            path = Path(tmp) / (name + ".java")
            path.write_text(source)
            result = subprocess.run([java, str(path)], capture_output=True, text=True, timeout=30)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_chat_is_queued_on_client_thread(self):
        for version in TARGETS:
            with self.subTest(version=version):
                path = ROOT / f"versions/{version}/project/src/main/java/org/xiaoxian/util/ChatUtil.java"
                source = path.read_text().split("public class ChatUtil", 1)[1]
                production_class = "static class ChatUtil" + source
                self.run_java("ChatThreadHarness", CHAT_HARNESS.replace("PRODUCTION_CLASS", production_class))

    def test_local_info_and_configured_count_precede_public_lookup(self):
        for version in TARGETS:
            with self.subTest(version=version):
                path = ROOT / f"versions/{version}/project/src/main/java/org/xiaoxian/lan/ShareToLan.java"
                source = path.read_text()
                method = "private void sendLanInfo" + source.split("private void sendLanInfo", 1)[1].split("private void startHttpApi", 1)[0]
                # Use the actual production resolver too: a double for this
                # helper could conceal a missing method in a version variant.
                self.assertIn("private static int resolveMaxPlayers", source)
                resolver = "private static int resolveMaxPlayers" + source.split("private static int resolveMaxPlayers", 1)[1].split("private static boolean isBlank", 1)[0]
                if version == "1.21.11":
                    self.assertIn('snapshot.putStatus("maxPlayer", String.valueOf(resolveMaxPlayers(server)))', source)
                self.run_java("LanInfoHarness", LAN_HARNESS.replace("PRODUCTION_METHOD", method).replace("PRODUCTION_RESOLVER", resolver))


if __name__ == "__main__":
    unittest.main()
