package org.xiaoxian;

import net.neoforged.neoforge.common.NeoForge;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;

/**
 * Holds every client only registration so that a dedicated server never loads a {@code Screen} class.
 */
final class ClientRegistrar {
    private ClientRegistrar() {
    }

    static void register(String customPort, String customMaxPlayer) {
        NeoForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        NeoForge.EVENT_BUS.register(new GuiShareToLanEdit());

        GuiShareToLanEdit.PortText = customPort;
        GuiShareToLanEdit.MaxPlayerText = customMaxPlayer;
    }
}
