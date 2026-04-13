package org.xiaoxian.easylan.forge.version;

public final class VersionBridgeResolver {
    private static final VersionBridge BRIDGE = createBridge();

    private VersionBridgeResolver() {
    }

    public static VersionBridge get() {
        return BRIDGE;
    }

    private static VersionBridge createBridge() {
        try {
            Class<?> bridgeClass = Class.forName("org.xiaoxian.easylan.forge.version.VersionBridgeImpl");
            return (VersionBridge) bridgeClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to load version bridge.", ex);
        }
    }
}
