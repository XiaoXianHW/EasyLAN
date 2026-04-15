package org.xiaoxian.easylan.neoforge.version;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "g", "f_11193_" };
    }
}
