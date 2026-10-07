"""Execute production 1.21.11 settings logic with deterministic Java doubles.

These tests cover actual arguments/getters and per-server state, not chat echoes.
They do not replace a transformed Minecraft client run.
"""
from pathlib import Path
import json
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / 'versions/1.21.11/project/src/main/java'
RESOURCE_ROOT = ROOT / 'versions/1.21.11/project/src/main/resources'


def method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def run_java(files, main):
    java_home = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None
    java = str(java_home / 'java') if java_home else 'java'
    javac = str(java_home / 'javac') if java_home else 'javac'
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        for name, source in files.items():
            path = root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(source)
        result = subprocess.run([javac, '-d', str(root), *map(str, root.rglob('*.java'))],
                                capture_output=True, text=True, timeout=30)
        if result.returncode:
            raise AssertionError(result.stdout + result.stderr)
        return subprocess.run([java, '-cp', str(root), main], capture_output=True,
                              text=True, timeout=30)


START_HARNESS = r'''
public class StartHarness {
    static class Component {
        int port;
        Component(int port) { this.port = port; }
        static Component translatable(String key) { return new Component(-1); }
    }
    static class PublishCommand { static Component getSuccessMessage(int port) { return new Component(port); } }
    static class EditBox { String value; EditBox(String v) { value=v; } String getValue() { return value; } }
    static class IntegratedServer {
        int port = -1, calls; boolean succeeds = true;
        boolean publishServer(Object mode, boolean commands, int requested) {
            calls++; if (succeeds) port = requested; return succeeds;
        }
        int getPort() { return port; }
    }
    static class Chat { Component result; void addMessage(Component r) { result=r; } }
    static class Gui { Chat chat = new Chat(); Chat getChat() { return chat; } }
    static class Minecraft {
        IntegratedServer server = new IntegratedServer(); Gui gui = new Gui();
        IntegratedServer getSingleplayerServer() { return server; }
        void setScreen(Object screen) {} void updateTitle() {}
    }
    static class EasyLAN { static String CustomPort, CustomMaxPlayer; }
    static class ConfigUtil { static int saves; static void save() { saves++; } }
    static class ShareToLan { static int setups; void handleLanSetup() { setups++; } }
    Minecraft minecraft = new Minecraft();
    EditBox portTextBox, maxPlayerTextBox = new EditBox("99");
    String PortText, MaxPlayerText; int publishPort = 37563;
    Object gameMode; boolean commands;
    PRODUCTION_METHOD
    public static void main(String[] args) {
        for (String input : new String[] {"25599", ""}) {
            StartHarness h = new StartHarness(); h.portTextBox = new EditBox(input); h.startLan();
            int expected = input.isEmpty() ? 37563 : 25599;
            if (h.minecraft.server.port != expected || h.minecraft.gui.chat.result.port != expected)
                throw new AssertionError("Requested, published and vanilla-message ports must agree");
            if (h.minecraft.server.calls != 1) throw new AssertionError("Publish exactly one listener");
        }
        StartHarness failed = new StartHarness(); failed.portTextBox = new EditBox("25599");
        failed.minecraft.server.succeeds = false; int setups = ShareToLan.setups;
        failed.startLan();
        if (ShareToLan.setups != setups || failed.minecraft.server.port != -1)
            throw new AssertionError("A bind failure must not report/apply a successful setup");
    }
}
'''

BRIDGE_FILES = {
    'org/xiaoxian/EasyLAN.java': '''package org.xiaoxian;
public class EasyLAN {
  public static class State { public String port="25599"; public void setLanPort(String v){port=v;} }
  private static final State STATE=new State(); public static State getRuntimeState(){return STATE;}
}''',
    'net/minecraft/client/server/IntegratedServer.java': '''package net.minecraft.client.server;
import org.xiaoxian.easylan.neoforge.version.ConfiguredPlayerLimit;
public class IntegratedServer implements ConfiguredPlayerLimit {
  public int limit=8, port=37563;
  public class PlayerList { public int getMaxPlayers(){return IntegratedServer.this.getMaxPlayers();} }
  private final PlayerList list=new PlayerList();
  public PlayerList getPlayerList(){return list;}
  public int getMaxPlayers(){return limit;} public int getPort(){return port;}
  public void easylan$setMaxPlayers(int value){limit=value;}
}''',
    'org/xiaoxian/easylan/neoforge/version/ReflectionVersionBridgeSupport.java': '''package org.xiaoxian.easylan.neoforge.version;
public abstract class ReflectionVersionBridgeSupport {
  public boolean setMaxPlayers(Object s,int n){throw new AssertionError("obsolete reflection path");}
  public int resolveMaxPlayers(Object s){return 0;} public String resolveLanPort(Object s){return null;}
  protected abstract String[] maxPlayerFieldNames();
}''',
    'BridgeHarness.java': '''import net.minecraft.client.server.IntegratedServer;
import org.xiaoxian.easylan.neoforge.version.VersionBridgeImpl;
public class BridgeHarness { public static void main(String[] args) {
  var bridge=new VersionBridgeImpl(); var server=new IntegratedServer(); var original=server.getPlayerList();
  if (!bridge.setMaxPlayers(server,99) || server.getMaxPlayers()!=99 || original.getMaxPlayers()!=99)
    throw new AssertionError("Server and admission must share effective limit");
  if (server.getPlayerList()!=original) throw new AssertionError("Do not replace live player list");
  if (!"37563".equals(bridge.resolveLanPort(server))) throw new AssertionError("Ignore stale input/cached port");
  server.port=-1;
  if (bridge.resolveLanPort(server)!=null || org.xiaoxian.EasyLAN.getRuntimeState().port!=null)
    throw new AssertionError("Unpublished server must clear stale port");
  if (bridge.setMaxPlayers(server,1) || server.getMaxPlayers()!=99) throw new AssertionError("Reject invalid limit");
  var second=new IntegratedServer(); if (bridge.resolveMaxPlayers(second)!=8) throw new AssertionError("New world leak");
} }'''
}

MIXIN_STUBS = {
    'net/minecraft/client/server/IntegratedServer.java': 'package net.minecraft.client.server; public class IntegratedServer {}',
    'org/spongepowered/asm/mixin/Mixin.java': 'package org.spongepowered.asm.mixin; public @interface Mixin { Class<?> value(); boolean remap(); }',
    'org/spongepowered/asm/mixin/Unique.java': 'package org.spongepowered.asm.mixin; public @interface Unique {}',
    'org/spongepowered/asm/mixin/injection/At.java': 'package org.spongepowered.asm.mixin.injection; public @interface At { String value(); }',
    'org/spongepowered/asm/mixin/injection/Inject.java': 'package org.spongepowered.asm.mixin.injection; public @interface Inject { String method(); At at(); boolean cancellable(); boolean remap(); }',
    'org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable.java': '''package org.spongepowered.asm.mixin.injection.callback;
public class CallbackInfoReturnable<T> { private T value; public CallbackInfoReturnable(T v){value=v;}
public void setReturnValue(T v){value=v;} public T getReturnValue(){return value;} }''',
    'MixinHarness.java': '''import org.xiaoxian.mixin.IntegratedServerMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class MixinHarness extends IntegratedServerMixin {
  int effective() throws Exception { var callback=new CallbackInfoReturnable<Integer>(8);
    var method=IntegratedServerMixin.class.getDeclaredMethod("easylan$resolveMaxPlayers",CallbackInfoReturnable.class);
    method.setAccessible(true);method.invoke(this,callback);return callback.getReturnValue(); }
  public static void main(String[] args) throws Exception {
    var first=new MixinHarness(); if(first.effective()!=8)throw new AssertionError("Preserve default");
    first.easylan$setMaxPlayers(99);if(first.effective()!=99)throw new AssertionError("Actual getter must return configured limit");
    first.easylan$setMaxPlayers(37);if(first.effective()!=37)throw new AssertionError("Repeated change");
    if(new MixinHarness().effective()!=8)throw new AssertionError("Limit leaked across worlds");
  }
}'''
}


class EffectiveSettings(unittest.TestCase):
    def test_publish_selected_port_and_negative_control(self):
        source = (JAVA_ROOT / 'org/xiaoxian/gui/GuiShareToLanEdit.java').read_text()
        production = method(source, 'private void startLan()').replace('org.xiaoxian.EasyLAN', 'EasyLAN')
        result = run_java({'StartHarness.java': START_HARNESS.replace('PRODUCTION_METHOD', production)}, 'StartHarness')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        # Reintroduce the exact bug: vanilla publishes the random suggestion.
        broken = production.replace('publishServer(gameMode, commands, selectedPort)',
                                    'publishServer(gameMode, commands, publishPort)')
        self.assertNotEqual(broken, production)
        result = run_java({'StartHarness.java': START_HARNESS.replace('PRODUCTION_METHOD', broken)}, 'StartHarness')
        self.assertNotEqual(result.returncode, 0, 'Negative control failed to detect random published port')

    def test_effective_server_bridge_and_stale_port(self):
        files = dict(BRIDGE_FILES)
        for name in ('VersionBridgeImpl', 'ConfiguredPlayerLimit'):
            rel = f'org/xiaoxian/easylan/neoforge/version/{name}.java'
            files[rel] = (JAVA_ROOT / rel).read_text()
        result = run_java(files, 'BridgeHarness')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_server_getter_override_and_negative_control(self):
        files = dict(MIXIN_STUBS)
        for rel in ('org/xiaoxian/mixin/IntegratedServerMixin.java',
                    'org/xiaoxian/easylan/neoforge/version/ConfiguredPlayerLimit.java'):
            files[rel] = (JAVA_ROOT / rel).read_text()
        result = run_java(files, 'MixinHarness')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        rel = 'org/xiaoxian/mixin/IntegratedServerMixin.java'
        files[rel] = files[rel].replace('callback.setReturnValue(easylan$maxPlayers);', '')
        result = run_java(files, 'MixinHarness')
        self.assertNotEqual(result.returncode, 0, 'Negative control failed to detect hardcoded server limit')

    def test_required_mixin_registration_and_no_second_listener(self):
        config = json.loads((RESOURCE_ROOT / 'easylan.mixins.json').read_text())
        self.assertTrue(config['required'])
        self.assertEqual(config['injectors']['defaultRequire'], 1)
        self.assertIn('IntegratedServerMixin', config['client'])
        self.assertIn('config="easylan.mixins.json"', (RESOURCE_ROOT / 'META-INF/neoforge.mods.toml').read_text())
        source = (JAVA_ROOT / 'org/xiaoxian/lan/ShareToLan.java').read_text()
        self.assertNotIn('openLanEndpoint', source)
        self.assertNotIn('startLanPort', source)
        api = method(source, 'private void updateApiInfo(')
        self.assertNotIn('GuiShareToLanEdit.PortText', api)
        self.assertIn('getLanPort(server)', api)


if __name__ == '__main__':
    unittest.main()
