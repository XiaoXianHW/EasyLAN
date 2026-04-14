package org.xiaoxian.easylan.fabric.version;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "field_72405_c", "f_11193_" };
    }
}
