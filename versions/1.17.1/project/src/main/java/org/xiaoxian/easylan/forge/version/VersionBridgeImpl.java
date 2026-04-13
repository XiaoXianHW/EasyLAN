package org.xiaoxian.easylan.forge.version;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "f_11193_" };
    }
}
