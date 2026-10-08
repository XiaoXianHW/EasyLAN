package org.xiaoxian.easylan.fabric.version;

import net.minecraft.client.server.IntegratedServer;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    public boolean setMaxPlayers(Object server, int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 500000
                || !(server instanceof IntegratedServer integratedServer)
                || !(server instanceof ConfiguredPlayerLimit configured)) {
            return false;
        }
        configured.easylan$setMaxPlayers(maxPlayers);
        return integratedServer.getMaxPlayers() == maxPlayers
                && integratedServer.getPlayerList().getMaxPlayers() == maxPlayers;
    }

    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "field_14347" };
    }
}
