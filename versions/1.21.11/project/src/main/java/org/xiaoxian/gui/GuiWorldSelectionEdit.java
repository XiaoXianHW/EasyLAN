package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;

public class GuiWorldSelectionEdit {
    public void onScreenInit(ScreenEvent.Init.Post event) {
        final Screen screen = event.getScreen();
        if (!(screen instanceof SelectWorldScreen)) {
            return;
        }

        event.addListener(Button.builder(Component.translatable("easylan.setting"),
                button -> Minecraft.getInstance().setScreen(new GuiEasyLanMain(screen)))
                .bounds(5, 5, 100, 20).build());
    }
}
