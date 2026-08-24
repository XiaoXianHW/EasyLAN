package org.xiaoxian.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;

public class EasyLanClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GuiShareToLanEdit.PortText = EasyLAN.CustomPort;
        GuiShareToLanEdit.MaxPlayerText = EasyLAN.CustomMaxPlayer;

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            GuiWorldSelectionEdit.maybeReplace(client, screen);
            GuiShareToLanEdit.maybeReplace(client, screen);
        });
    }
}
