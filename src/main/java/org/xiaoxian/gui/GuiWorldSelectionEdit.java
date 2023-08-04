package org.xiaoxian.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        Screen guiScreen = event.getScreen();
        if (guiScreen instanceof SelectWorldScreen) {
            event.setNewScreen(new GuiWorldSelectionModified(event.getScreen()));
        }
    }

    public static class GuiWorldSelectionModified extends SelectWorldScreen {

        public GuiWorldSelectionModified(Screen parentScreen) {
            super(new TitleScreen());
        }

        @Override
        protected void init() {
            Button button = Button.builder(Component.translatable("easylan.setting"), (p_96660_) -> {
                assert GuiWorldSelectionModified.this.minecraft != null;
                GuiWorldSelectionModified.this.minecraft.setScreen(new GuiEasyLanMain(this));
            }).bounds(5, 5, 100, 20).build();
            this.addRenderableWidget(button);

            super.init();
        }
    }
}
