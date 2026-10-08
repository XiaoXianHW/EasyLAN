#!/usr/bin/env python3
"""Offline source regression guards; not a Minecraft build or gameplay test.

Run: python3 -m unittest discover -s tests -v
These deliberately guard the production input/rendering and rule-wiring paths
that caused the August 2026 v1.6a failures. Run the manual matrix before release.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
GROUPS = sorted(p for p in ROOT.glob("shared/fabric-group-*")
                if (p / "src/main/java/org/xiaoxian/lan/ServerRuleApplier.java").exists())

class ReportedRegressionGuards(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        expected = 3 if (ROOT / "versions/1.20.6").exists() else 1
        if len(GROUPS) != expected:
            raise AssertionError(f"Expected {expected} Fabric source families, found {len(GROUPS)}")

    def test_world_rules_wait_for_server_started(self):
        entrypoints = list(ROOT.glob("shared/fabric-group-*/src/main/java/org/xiaoxian/EasyLAN.java"))
        self.assertTrue(entrypoints)
        for path in entrypoints:
            with self.subTest(entrypoint=path):
                source = path.read_text()
                self.assertIn("SERVER_STARTING.register(ServerStarting::onServerStarting)", source)
                self.assertIn("SERVER_STARTED.register(ServerStarting::onServerStarted)", source)
        for group in GROUPS:
            with self.subTest(group=group.name):
                source = (group / "src/main/java/org/xiaoxian/lan/ServerStarting.java").read_text()
                early, ready = source.split("public static void onServerStarted", 1)
                self.assertNotIn("ServerRuleApplier.apply(", early)
                self.assertIn("ServerRuleApplier.apply(minecraftServer);", ready)
                self.assertIn("WhitelistCommand.register(dispatcher)", early)

    def test_motd_uses_remappable_typed_api(self):
        for group in GROUPS:
            with self.subTest(group=group.name):
                source = (group / "src/main/java/org/xiaoxian/lan/ServerRuleApplier.java").read_text()
                self.assertRegex(source, r'(?:server|minecraftServer)\.setMotd\(motd(?: == null \? "" : motd)?\);')
                self.assertNotIn('"setMotd"', source)

    def test_motd_reapplied_after_vanilla_lan_publish(self):
        for group in GROUPS:
            with self.subTest(group=group.name):
                source = (group / "src/main/java/org/xiaoxian/lan/ShareToLan.java").read_text()
                self.assertIn("ServerRuleApplier.apply(server);", source)
                gui = (group / "src/main/java/org/xiaoxian/gui/GuiShareToLanEdit.java").read_text()
                self.assertLess(gui.index("if (!publish"), gui.index("new ShareToLan().handleLanSetup();"))

    def test_modern_world_creation_not_injected(self):
        if not (ROOT / "versions/1.20.6").exists():
            self.skipTest("modern UI regression only")
        for group in GROUPS:
            with self.subTest(group=group.name):
                gui = (group / "src/main/java/org/xiaoxian/gui/GuiWorldSelectionEdit.java").read_text()
                client = (group / "src/main/java/org/xiaoxian/client/EasyLanClient.java").read_text()
                self.assertNotIn("CreateWorld", gui + client)
                self.assertIn("SelectWorldScreen", gui)
                self.assertIn('Component.translatable("easylan.setting")', gui)

    def test_modern_inputs_have_one_event_owner(self):
        if not (ROOT / "versions/1.20.6").exists():
            self.skipTest("modern UI regression only")
        for group in GROUPS:
            for filename, fields in [("GuiEasyLanMain", ["motdTextBox"]), ("GuiShareToLanEdit", ["PortTextBox", "MaxPlayerBox"])]:
                with self.subTest(group=group.name, screen=filename):
                    source = (group / f"src/main/java/org/xiaoxian/gui/{filename}.java").read_text()
                    for field in fields:
                        self.assertEqual(source.count(f"addRenderableWidget({field});"), 1)
                        self.assertIn(f"{field}.setResponder(", source)
                        self.assertNotRegex(source, rf"{field}\.(?:charTyped|keyPressed|mouseClicked|render)\(")
                    self.assertNotRegex(source, r"public boolean (?:charTyped|keyPressed|mouseClicked)\(")

if __name__ == "__main__":
    unittest.main()
