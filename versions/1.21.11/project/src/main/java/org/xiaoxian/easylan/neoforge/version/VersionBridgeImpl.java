package org.xiaoxian.easylan.neoforge.version;

import net.minecraft.client.server.IntegratedServer;
import org.xiaoxian.EasyLAN;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    public boolean setMaxPlayers(Object server, int maxPlayers) {
        if (!(server instanceof IntegratedServer integratedServer)
                || !(server instanceof ConfiguredPlayerLimit configured)
                || maxPlayers < 2 || maxPlayers > 500000) {
            return false;
        }

        // 1.21.11 moved the limit to IntegratedServer.getMaxPlayers(). Changing
        // only PlayerList leaves the vanilla status response at eight players.
        configured.easylan$setMaxPlayers(maxPlayers);
        return integratedServer.getMaxPlayers() == maxPlayers
                && integratedServer.getPlayerList().getMaxPlayers() == maxPlayers;
    }

    @Override
    public int resolveMaxPlayers(Object server) {
        return server instanceof IntegratedServer integratedServer
                ? integratedServer.getMaxPlayers() : super.resolveMaxPlayers(server);
    }

    @Override
    public String resolveLanPort(Object server) {
        if (!(server instanceof IntegratedServer integratedServer)) {
            return super.resolveLanPort(server);
        }
        int port = integratedServer.getPort();
        String publishedPort = port > 0 ? String.valueOf(port) : null;
        EasyLAN.getRuntimeState().setLanPort(publishedPort);
        return publishedPort;
    }

    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[0];
    }
}
