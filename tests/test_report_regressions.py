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

    def test_12111_uses_per_event_listener_buses(self):
        folder = ROOT / "versions/1.21.11/project/src/main/java/org/xiaoxian"
        source = (folder / "EasyLAN.java").read_text()
        self.assertIn("ServerStartingEvent.BUS.addListener", source)
        self.assertIn("ServerStoppingEvent.BUS.addListener", source)
        source += (folder / "ClientRegistrar.java").read_text()
        self.assertIn("ScreenEvent.Init.Post.BUS.addListener", source)
        self.assertIn("ScreenEvent.Opening.BUS.addListener", source)
        self.assertNotIn("EVENT_BUS.register", source)

if __name__ == "__main__":
    unittest.main()
