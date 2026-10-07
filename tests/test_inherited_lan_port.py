"""Exercise the actual inherited-screen port helper without Minecraft GUI.

All five source variants must contain the same tested implementation. Runtime
acceptance still has to verify vanilla's real responder and published endpoint.
"""
from pathlib import Path
import unittest
from test_forge_12111_settings import method, run_java

ROOT = Path(__file__).resolve().parents[1]
VERSIONS = ("1.19.4", "1.20.1", "1.20.6", "1.21.1", "1.21.5")

HARNESS = r'''
public class InheritedPortHarness {
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
    static class Button {
        boolean active = true;
        Runnable action;
        void onPress() { action.run(); }
    }
    static class EditBox {
        java.util.function.Consumer<String> responder;
        void setValue(String value) { responder.accept(value); }
    }
    static class State {
        String port = "stale-input";
        void setLanPort(String value) { port = value; }
    }
    static class EasyLAN {
        static State state = new State();
        static State getRuntimeState() { return state; }
    }
    EditBox vanillaPortTextBox;
    String PortText, PortWarningText = "";
    int vanillaPort = 37563, publishes;
    boolean available = true, bindSucceeds = true, ignoreSelectedPort;
    Button original = new Button();
    InheritedPortHarness(String input) {
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
        for (String input : new String[] {"25599", ""}) {
            var h = new InheritedPortHarness(input);
            if (!h.publishVanillaPort(h.original)) throw new AssertionError("Valid publication rejected");
            int expected = input.isEmpty() ? 37563 : 25599;
            if (Minecraft.instance.server.port != expected || h.publishes != 1)
                throw new AssertionError("Vanilla must publish requested port exactly once");
        }
        var fail = new InheritedPortHarness("25599"); fail.bindSucceeds = false;
        if (fail.publishVanillaPort(fail.original)) throw new AssertionError("Bind failure reported success");
        var unavailable = new InheritedPortHarness("25599"); unavailable.available = false;
        if (unavailable.publishVanillaPort(unavailable.original) || unavailable.publishes != 0)
            throw new AssertionError("Disabled vanilla action must not be called");
        var missing = new InheritedPortHarness("25599"); missing.vanillaPortTextBox = null;
        if (missing.publishVanillaPort(missing.original) || missing.publishes != 0)
            throw new AssertionError("Missing responder must not publish a random port");
        var mismatch = new InheritedPortHarness("25599"); mismatch.ignoreSelectedPort = true;
        if (mismatch.publishVanillaPort(mismatch.original))
            throw new AssertionError("Wrong actual port must not start EasyLAN success/API setup");
        var server = Minecraft.instance.server;
        server.port = 25599;
        if (!"25599".equals(getLanPort(server))) throw new AssertionError("Ignore stale cached port");
        server.port = -1;
        if (getLanPort(server) != null || EasyLAN.state.port != null)
            throw new AssertionError("Unpublished endpoint must clear stale state");
    }
}
'''


class InheritedLanPort(unittest.TestCase):
    def test_production_helpers_and_negative_control(self):
        implementations = set()
        for version in VERSIONS:
            base = ROOT / f"versions/{version}/project/src/main/java/org/xiaoxian"
            gui = (base / "gui/GuiShareToLanEdit.java").read_text()
            lan = (base / "lan/ShareToLan.java").read_text()
            publish = method(gui, "private boolean publishVanillaPort(")
            port = method(lan, "private static String getLanPort(")
            implementations.add((publish, port))
        self.assertEqual(len(implementations), 1, "Each source variant must use the tested helper")
        publish, port = implementations.pop()
        harness = HARNESS.replace("PUBLISH_METHOD", publish).replace("PORT_METHOD", port)
        result = run_java({"InheritedPortHarness.java": harness}, "InheritedPortHarness")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        broken = harness.replace("vanillaPortTextBox.setValue(PortText);", "/* original port bug */")
        result = run_java({"InheritedPortHarness.java": broken}, "InheritedPortHarness")
        self.assertNotEqual(result.returncode, 0, "Negative control missed the random-port defect")

    def test_source_integration_for_all_five_versions(self):
        for version in VERSIONS:
            with self.subTest(version=version):
                base = ROOT / f"versions/{version}/project/src/main/java/org/xiaoxian"
                gui = (base / "gui/GuiShareToLanEdit.java").read_text()
                lan = (base / "lan/ShareToLan.java").read_text()
                self.assertIn("vanillaPortTextBox = targetEditBox;", gui)
                self.assertIn("vanillaPortTextBox = null;", gui)
                self.assertRegex(gui, r"if \(!publishVanillaPort\(finalOriginalButton\)\)\s*\{\s*return;")
                self.assertNotIn("startLanPort", lan)
                self.assertNotIn("openLanEndpoint", lan)
                self.assertNotIn("GuiShareToLanEdit.PortText", method(lan, "private void updateApiInfo("))


if __name__ == "__main__":
    unittest.main()
