package org.xiaoxian.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof SelectWorldScreen) {
            event.setNewScreen(new GuiWorldSelectionModified(new TitleScreen()));
        }
    }

    public static class GuiWorldSelectionModified extends SelectWorldScreen {

        public GuiWorldSelectionModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void init() {
            super.init();
            this.addRenderableWidget(new Button(5, 5, 100, 20, Component.nullToEmpty(I18n.get("easylan.setting")), (button) -> {
                GuiWorldSelectionModified.this.minecraft.setScreen(new GuiEasyLanMain(this));
            }));
        }
    }
}
