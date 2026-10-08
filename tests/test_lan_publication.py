"""Execute production publication/getter helpers with Java test doubles.

These deterministic tests include negative controls, but do not replace a
transformed Minecraft client run or actual LAN discovery acceptance.
"""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
GROUPS = sorted(p for p in ROOT.glob('shared/fabric-group-*')
                if (p / 'src/main/java/org/xiaoxian/lan/ShareToLan.java').exists())


def method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth, end = 1, opening + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def run_java(files, main):
    home = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None
    javac, java = ((str(home / 'javac'), str(home / 'java')) if home else ('javac', 'java'))
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
        return subprocess.run([java, '-cp', str(root), main], capture_output=True, text=True, timeout=30)


INHERITED = r'''
public class Harness {
    static class IntegratedServer {
        int port = -1; boolean published;
        boolean isPublished() { return published; }
        int getPort() { return port; }
    }
    static class Minecraft {
        static Minecraft instance;
        IntegratedServer server = new IntegratedServer();
        static Minecraft getInstance() { return instance; }
        IntegratedServer getSingleplayerServer() { return server; }
    }
    static class I18n { static String get(String key) { return key; } }
    static class KeyEvent { KeyEvent(int a, int b, int c) {} }
    static class Button {
        boolean active = true; Runnable action;
        void onPress() { action.run(); }
        void onPress(KeyEvent event) { action.run(); }
    }
    static class EditBox {
        java.util.function.Consumer<String> responder;
        void setValue(String value) { responder.accept(value); }
    }
    static class State { String port = "stale"; void setLanPort(String v) { port = v; } }
    static class EasyLAN { static State state = new State(); static State getRuntimeState() { return state; } }
    EditBox vanillaPortTextBox;
    String PortText, PortWarningText = "";
    int vanillaPort = 37563, publishes;
    boolean available = true, bindSucceeds = true, ignoreSelectedPort;
    Button original = new Button();
    Harness(String input) {
        PortText = input;
        Minecraft.instance = new Minecraft();
        vanillaPortTextBox = new EditBox();
        vanillaPortTextBox.responder = value -> {
            vanillaPort = value.isEmpty() ? 37563 : Integer.parseInt(value);
            original.active = available;
        };
        original.action = () -> {
            publishes++;
            if (bindSucceeds) {
                Minecraft.instance.server.port = ignoreSelectedPort ? 37563 : vanillaPort;
                Minecraft.instance.server.published = true;
            }
        };
    }
    PUBLISH_METHOD
    PORT_METHOD
    public static void main(String[] args) {
        for (String input : new String[] {"25599", "02559", ""}) {
            Harness h = new Harness(input);
            if (!h.publishVanillaPort(h.original)) throw new AssertionError("Valid publication rejected");
            int expected = input.isEmpty() ? 37563 : Integer.parseInt(input);
            if (Minecraft.instance.server.port != expected || h.publishes != 1)
                throw new AssertionError("Publish the requested port exactly once");
            if (h.publishVanillaPort(h.original) || h.publishes != 1)
                throw new AssertionError("Repeated click published again");
        }
        Harness fail = new Harness("25599"); fail.bindSucceeds = false;
        if (fail.publishVanillaPort(fail.original)) throw new AssertionError("Bind failure reported success");
        Harness unavailable = new Harness("25599"); unavailable.available = false;
        if (unavailable.publishVanillaPort(unavailable.original) || unavailable.publishes != 0)
            throw new AssertionError("Disabled action called");
        Harness missing = new Harness("25599"); missing.vanillaPortTextBox = null;
        if (missing.publishVanillaPort(missing.original) || missing.publishes != 0)
            throw new AssertionError("Missing port control must fail closed");
        Harness mismatch = new Harness("25599"); mismatch.ignoreSelectedPort = true;
        if (mismatch.publishVanillaPort(mismatch.original)) throw new AssertionError("Wrong live port accepted");
        IntegratedServer server = Minecraft.instance.server;
        server.port = 25599;
        if (!"25599".equals(getLanPort(server))) throw new AssertionError("Stale cache won over live port");
        server.published = false;
        if (getLanPort(server) != null || EasyLAN.state.port != null)
            throw new AssertionError("Unpublished endpoint kept stale state");
    }
}
'''

LEGACY = r'''
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
class ShareToLanScreen {
    private static String ignoredStatic = "wrong";
    private String selectedMode = "creative";
    private boolean selectedCommands = true;
}
public class Harness extends ShareToLanScreen {
    static class Component { String key; int port; Component(String k, int p) { key=k; port=p; } }
    static class TranslatableComponent extends Component {
        TranslatableComponent(String k, Object... values) { super(k, values.length == 0 ? -1 : (Integer) values[0]); }
    }
    static class GameType { static String byName(String mode) { return mode; } }
    static class HttpUtil { static int getAvailablePort() { return 37563; } }
    static class I18n { static String get(String key) { return key; } }
    static class IntegratedServer {
        int port = -1, calls; boolean succeeds = true, published, commands; String mode;
        boolean publishServer(String m, boolean c, int p) {
            calls++; mode=m; commands=c;
            if (succeeds) { port=p; published=true; }
            return succeeds;
        }
        int getPort() { return port; }
        boolean isPublished() { return published; }
    }
    static class Chat { Component result; void addMessage(Component r) { result=r; } }
    static class Gui { Chat chat = new Chat(); Chat getChat() { return chat; } }
    static class Minecraft {
        static Minecraft instance;
        IntegratedServer server = new IntegratedServer(); Gui gui = new Gui();
        static Minecraft getInstance() { return instance; }
        IntegratedServer getSingleplayerServer() { return server; }
        void setScreen(Object s) {} void updateTitle() {}
    }
    static class State { String port = "stale"; void setLanPort(String v) { port=v; } }
    static class EasyLAN { static State state = new State(); static State getRuntimeState() { return state; } }
    Minecraft minecraft = new Minecraft();
    String PortText, PortWarningText;
    Harness(String input) { PortText=input; Minecraft.instance=minecraft; }
    PUBLISH_METHOD
    OPTION_METHOD
    PORT_METHOD
    public static void main(String[] args) {
        for (String input : new String[] {"25599", "02559", ""}) {
            Harness h = new Harness(input);
            if (!h.publishSelectedPort()) throw new AssertionError("Publish failed");
            int expected = input.isEmpty() ? 37563 : Integer.parseInt(input);
            IntegratedServer s=h.minecraft.server;
            if (s.port != expected || h.minecraft.gui.chat.result.port != expected || s.calls != 1)
                throw new AssertionError("Requested, advertised and vanilla message ports must agree");
            if (!s.commands || !"creative".equals(s.mode)) throw new AssertionError("Lost vanilla selections");
            if (h.publishSelectedPort() || s.calls != 1) throw new AssertionError("Repeated click published again");
        }
        Harness fail = new Harness("25599"); fail.minecraft.server.succeeds=false;
        if (fail.publishSelectedPort() || !"commands.publish.failed".equals(fail.minecraft.gui.chat.result.key))
            throw new AssertionError("Failed publication accepted");
        Harness none = new Harness("25599"); none.minecraft.server=null;
        if (none.publishSelectedPort()) throw new AssertionError("No server accepted");
        Harness h = new Harness("25599"); h.publishSelectedPort();
        if (!"25599".equals(getLanPort(h.minecraft.server))) throw new AssertionError("Stale cache returned");
        h.minecraft.server.published=false;
        if (getLanPort(h.minecraft.server) != null || EasyLAN.state.port != null)
            throw new AssertionError("Unpublished endpoint kept stale state");
    }
}
'''


class LanPublication(unittest.TestCase):
    def test_production_helpers_and_negative_controls(self):
        for group in GROUPS:
            with self.subTest(group=group.name):
                base = group / 'src/main/java/org/xiaoxian'
                gui = (base / 'gui/GuiShareToLanEdit.java').read_text()
                lan = (base / 'lan/ShareToLan.java').read_text()
                inherited = 'publishVanillaPort' in gui
                template = INHERITED if inherited else LEGACY
                publish = method(gui, 'private boolean publishVanillaPort(' if inherited else 'private boolean publishSelectedPort(')
                harness = template.replace('PUBLISH_METHOD', publish).replace('PORT_METHOD', method(lan, 'private static String getLanPort('))
                if not inherited:
                    harness = harness.replace('OPTION_METHOD', method(gui, 'private Object readVanillaOption('))
                result = run_java({'Harness.java': harness}, 'Harness')
                self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
                if inherited:
                    broken = harness.replace('vanillaPortTextBox.setValue(PortText);', '/* original random port bug */')
                else:
                    broken = harness.replace('PortText.isEmpty() ? HttpUtil.getAvailablePort() : Integer.parseInt(PortText)', 'HttpUtil.getAvailablePort()')
                result = run_java({'Harness.java': broken}, 'Harness')
                self.assertNotEqual(result.returncode, 0, 'Negative control missed the random-port defect')

    def test_production_wiring_uses_live_settings(self):
        for group in GROUPS:
            with self.subTest(group=group.name):
                base = group / 'src/main/java/org/xiaoxian'
                gui = (base / 'gui/GuiShareToLanEdit.java').read_text()
                lan = (base / 'lan/ShareToLan.java').read_text()
                self.assertNotIn('startLanPort', lan)
                self.assertNotIn('openLanEndpoint', lan)
                self.assertNotIn('GuiShareToLanEdit.PortText', method(lan, 'private void updateApiInfo('))
                self.assertIn('server.getPlayerList().getMaxPlayers()', method(lan, 'private void updateApiInfo('))
                self.assertIn('server.getPlayerList().getMaxPlayers() != maxPlayers', method(lan, 'private void setMaxPlayer('))
                self.assertIn('server == null || !server.isPublished()', method(lan, 'public void handleLanSetup('))
                if 'publishVanillaPort' in gui:
                    self.assertIn('vanillaPortTextBox = null;', gui)
                    self.assertIn('vanillaPortTextBox = targetEditBox;', gui)
                    self.assertRegex(gui, r'if \(!publishVanillaPort\(finalOriginalButton\)\)\s*\{\s*return;')
                else:
                    self.assertRegex(gui, r'if \(!publishSelectedPort\(\)\)\s*\{\s*return;')

if __name__ == '__main__':
    unittest.main()
