package org.xiaoxian.easylan.fabric.version;

import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;

public interface VersionBridge {
    void openLanEndpoint(Object connection, int port) throws IOException;

    boolean setMaxPlayers(Object server, int maxPlayers);

    String resolveLanPort(Object server);

    Screen resolveWorldSelectionParent(Screen screen);

    Screen resolveShareToLanParent(Screen screen);
}