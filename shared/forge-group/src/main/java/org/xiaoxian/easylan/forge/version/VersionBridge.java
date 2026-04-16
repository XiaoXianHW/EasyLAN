package org.xiaoxian.easylan.forge.version;

import java.io.IOException;

public interface VersionBridge {
    void openLanEndpoint(Object connection, int port) throws IOException;

    boolean setMaxPlayers(Object server, int maxPlayers);

    int resolveMaxPlayers(Object server);

    String resolveLanPort(Object server);
}
