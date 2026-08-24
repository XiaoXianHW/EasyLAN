package org.xiaoxian.easylan.forge.version;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "f_11193_" };
    }

    @Override
    protected String[] playerListMethodNames() {
        return new String[] { "getPlayerList", "m_6846_" };
    }
}
