package org.xiaoxian.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        Screen guiScreen = event.getGui();
        if (guiScreen instanceof SelectWorldScreen) {
            event.setGui(new GuiWorldSelectionModified(event.getGui()));
        }
    }

    public static class GuiWorldSelectionModified extends SelectWorldScreen {

        public GuiWorldSelectionModified(Screen parentScreen) {
            super(new TitleScreen());
        }

        @Override
        protected void init() {
            this.addRenderableWidget(new Button(5, 5, 100, 20, Component.nullToEmpty(I18n.get("easylan.setting")), (button) -> {
                assert GuiWorldSelectionModified.this.minecraft != null;
                GuiWorldSelectionModified.this.minecraft.setScreen(new GuiEasyLanMain(this));
            }));
            super.init();
        }
    }
}
