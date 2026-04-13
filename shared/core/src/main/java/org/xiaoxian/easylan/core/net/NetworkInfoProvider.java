package org.xiaoxian.easylan.core.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NetworkInfoProvider {
    private NetworkInfoProvider() {
    }

    public static String getPublicIPv4() {
        return readRemoteValue("https://easylan.xiaoxian.org/api/myip", "ip", "Unknown");
    }

    public static boolean checkIpIsPublic() {
        return "true".equals(readRemoteValue("https://easylan.xiaoxian.org/api/myipcheck", "public", "false"));
    }

    public static String getLocalIpv4() {
        InetAddress address = getLocalAddress(false);
        return address == null ? "Unknown" : address.getHostAddress();
    }

    public static String getLocalIpv6() {
        InetAddress address = getLocalAddress(true);
        return address == null ? "Unknown" : address.getHostAddress();
    }

    private static String readRemoteValue(String address, String key, String fallback) {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) {
                return fallback;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject object = new Gson().fromJson(response.toString(), JsonObject.class);
            return object.has(key) ? object.get(key).getAsString() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static InetAddress getLocalAddress(boolean preferIpv6) {
        List<InetAddress> addresses = new ArrayList<>();
        Set<String> virtualKeywords = new HashSet<>(Arrays.asList("vmware", "virtual", "hyper-v", "vbox", "virtualbox"));

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                String displayName = networkInterface.getDisplayName();
                if (displayName == null) {
                    continue;
                }

                String lowerName = displayName.toLowerCase();
                boolean isVirtual = false;
                for (String keyword : virtualKeywords) {
                    if (lowerName.contains(keyword)) {
                        isVirtual = true;
                        break;
                    }
                }
                if (isVirtual) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        addresses.add(inetAddress);
                    }
                }
            }
        } catch (SocketException ignored) {
            return null;
        }

        InetAddress ipv4 = null;
        InetAddress ipv6 = null;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address && ipv4 == null && isPrivateIpv4(address)) {
                ipv4 = address;
            }
            if (address instanceof Inet6Address && ipv6 == null && isValidIpv6(address)) {
                ipv6 = address;
            }
        }

        return preferIpv6 ? ipv6 : ipv4;
    }

    private static boolean isPrivateIpv4(InetAddress address) {
        String ip = address.getHostAddress();
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }

    private static boolean isValidIpv6(InetAddress address) {
        String ip = address.getHostAddress();
        return !ip.contains("%") && !ip.matches(".*:0:0:0:0:0:.*") && !ip.matches(".*::.*");
    }
}
