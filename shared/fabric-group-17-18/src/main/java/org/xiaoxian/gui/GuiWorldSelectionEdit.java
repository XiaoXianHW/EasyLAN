package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
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
            this.addRenderableWidget(new Button(5, 5, 100, 20, Component.nullToEmpty("EasyLAN"), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new GuiEasyLanMain(this));
                }
            }));
        }
    }
}
