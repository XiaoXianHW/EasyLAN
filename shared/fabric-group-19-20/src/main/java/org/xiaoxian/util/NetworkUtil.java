package org.xiaoxian.util;

import org.xiaoxian.easylan.core.net.NetworkInfoProvider;

public class NetworkUtil {
    public static String getPublicIPv4() {
        return NetworkInfoProvider.getPublicIPv4();
    }

    public static boolean checkIpIsPublic() {
        return NetworkInfoProvider.checkIpIsPublic();
    }

    public static String getLocalIpv4() {
        return NetworkInfoProvider.getLocalIpv4();
    }

    public static String getLocalIpv6() {
        return NetworkInfoProvider.getLocalIpv6();
    }
}