package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;
import org.xiaoxian.easylan.fabric.version.VersionBridgeResolver;

public class GuiWorldSelectionEdit {
    public static void maybeReplace(Minecraft minecraft, Screen screen) {
        if (screen instanceof SelectWorldScreen && !(screen instanceof GuiWorldSelectionModified)) {
            Screen parentScreen = VersionBridgeResolver.get().resolveWorldSelectionParent(screen);
            minecraft.setScreen(new GuiWorldSelectionModified(parentScreen));
        }
    }

    public static class GuiWorldSelectionModified extends SelectWorldScreen {
        public GuiWorldSelectionModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void init() {
            super.init();
            this.addButton(new Button(5, 5, 100, 20, I18n.get("easylan.setting"), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new GuiEasyLanMain(this));
                }
            }));
        }
    }
}
