"""Focused source-contract guards; these do not replace Minecraft runtime QA."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ReportRegressions(unittest.TestCase):
    def test_settings_entry_is_only_on_world_selection(self):
        paths = list(ROOT.glob("**/GuiWorldSelectionEdit.java"))
        self.assertTrue(paths)
        for path in paths:
            source = path.read_text()
            with self.subTest(path=str(path.relative_to(ROOT))):
                self.assertNotIn("CreateWorldScreen", source)
                if "GuiWorldSelectionModified extends SelectWorldScreen" in source:
                    self.assertIn("if (event.getScreen() instanceof SelectWorldScreen)", source)
                else:
                    self.assertRegex(source, r"if \(\!\(screen instanceof SelectWorldScreen\)\)\s*\{\s*return;")

    def test_input_is_dispatched_only_by_the_screen(self):
        for path in ROOT.glob("**/Gui*java"):
            source = path.read_text()
            with self.subTest(path=str(path.relative_to(ROOT))):
                self.assertNotRegex(source, r"(?:MotdTextBox|PortTextBox|MaxPlayerBox)\.(?:charTyped|keyPressed)\(")

    def test_settings_background_cannot_retain_previous_frame(self):
        backgrounds = [p for p in ROOT.glob("**/GuiEasyLanMain.java") if "matrixStack.fill(0, 0" in p.read_text()]
        self.assertTrue(backgrounds)
        for path in backgrounds:
            source = path.read_text()
            with self.subTest(path=str(path.relative_to(ROOT))):
                self.assertIn("matrixStack.fill(0, 0, this.width, this.height, 0xFF101010)", source)

    def test_local_chat_precedes_external_network_in_every_override(self):
        paths = list(ROOT.glob("**/ShareToLan.java"))
        self.assertGreaterEqual(len(paths), 2)
        for path in paths:
            source = path.read_text()
            method = source.split("private void sendLanInfo", 1)[1].split("private void startHttpApi", 1)[0]
            with self.subTest(path=str(path.relative_to(ROOT))):
                public_call = method.index("NetworkUtil.getPublicIPv4()")
                for required in ["Successfully", "easylan.text.port", "easylan.text.maxplayer", "easylan.text.onlineMode", "/status", "/playerlist"]:
                    self.assertLess(method.index(required), public_call)

    def test_chat_is_enqueued_on_client_thread(self):
        source = (ROOT / "shared/neoforge-group/src/main/java/org/xiaoxian/util/ChatUtil.java").read_text()
        self.assertLess(source.index("minecraft.execute(() ->"), source.index(".getChat().addMessage(component)"))

if __name__ == "__main__":
    unittest.main()
