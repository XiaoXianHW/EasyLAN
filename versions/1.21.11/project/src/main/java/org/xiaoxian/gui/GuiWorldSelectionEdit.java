package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        final Screen screen = event.getScreen();
        // CreateWorldScreen is opened directly when the player has no world yet, so it needs the button too.
        if (!(screen instanceof SelectWorldScreen) && !(screen instanceof CreateWorldScreen)) {
            return;
        }

        event.addListener(Button.builder(Component.translatable("easylan.setting"),
                button -> Minecraft.getInstance().setScreen(new GuiEasyLanMain(screen)))
                .bounds(5, 5, 100, 20).build());
    }
}
