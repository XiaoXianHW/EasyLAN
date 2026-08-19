package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;

/**
 * Holds every client only registration so that a dedicated server never loads a screen class.
 */
final class ClientRegistrar {
    private ClientRegistrar() {
    }

    static void register(String customPort, String customMaxPlayer) {
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());

        GuiShareToLanEdit.PortText = customPort;
        GuiShareToLanEdit.MaxPlayerText = customMaxPlayer;
    }
}
