package org.xiaoxian;

import net.minecraftforge.client.event.ScreenEvent;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;

/**
 * Holds every client only registration so that a dedicated server never loads a screen class.
 */
final class ClientRegistrar {
    private ClientRegistrar() {
    }

    static void register(String customPort, String customMaxPlayer) {
        ScreenEvent.Init.Post.BUS.addListener(new GuiWorldSelectionEdit()::onScreenInit);
        ScreenEvent.Opening.BUS.addListener(new GuiShareToLanEdit()::onGuiOpenEvent);

        GuiShareToLanEdit.PortText = customPort;
        GuiShareToLanEdit.MaxPlayerText = customMaxPlayer;
    }
}
