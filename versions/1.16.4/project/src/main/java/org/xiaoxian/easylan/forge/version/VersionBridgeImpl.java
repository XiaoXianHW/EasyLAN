package org.xiaoxian.easylan.forge.version;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "field_72405_c" };
    }

    @Override
    protected String[] playerListMethodNames() {
        return new String[] { "getPlayerList", "func_184103_al" };
    }
}
