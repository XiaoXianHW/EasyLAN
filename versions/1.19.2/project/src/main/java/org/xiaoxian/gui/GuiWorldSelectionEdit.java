package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        final Screen screen = event.getScreen();
        if (!(screen instanceof SelectWorldScreen)) {
            return;
        }

        event.addListener(new Button(5, 5, 100, 20, Component.nullToEmpty(I18n.get("easylan.setting")),
                button -> Minecraft.getInstance().setScreen(new GuiEasyLanMain(screen))));
    }
}
