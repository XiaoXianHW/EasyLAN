package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public class GuiWorldSelectionEdit {
    public static void maybeReplace(Minecraft minecraft, Screen screen) {
        if (screen instanceof SelectWorldScreen && !(screen instanceof GuiWorldSelectionModified)) {
            minecraft.setScreen(new GuiWorldSelectionModified(new TitleScreen()));
        }
    }

    public static class GuiWorldSelectionModified extends SelectWorldScreen {

        public GuiWorldSelectionModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void init() {
            super.init();
            Button button = Button.builder(Component.translatable("easylan.setting"), (p_96660_) -> {
                assert GuiWorldSelectionModified.this.minecraft != null;
                GuiWorldSelectionModified.this.minecraft.setScreen(new GuiEasyLanMain(this));
            }).bounds(5, 5, 100, 20).build();
            this.addRenderableWidget(button);
        }
    }
}
