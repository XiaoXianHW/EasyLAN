package org.xiaoxian.easylan.fabric.version;

import java.io.IOException;

public interface VersionBridge {
    void openLanEndpoint(Object connection, int port) throws IOException;

    boolean setMaxPlayers(Object server, int maxPlayers);

    String resolveLanPort(Object server);
}
