package org.xiaoxian.util;

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

public class NetworkUtil {
    public static String getPublicIPv4() {
        try {
            URL url = new URL("https://easylan.xiaoxian.org/api/myip");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                return jsonObject.get("ip").getAsString();
            }

            return "Unknown";
        } catch (Exception ex) {
            return "Unknown";
        }
    }

    public static boolean checkIpIsPublic() {
        try {
            URL url = new URL("https://easylan.xiaoxian.org/api/myipcheck");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                return "true".equals(jsonObject.get("public").getAsString());
            }

            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getLocalIpv4() {
        InetAddress address = getLocalAddress(false);
        return address != null ? address.getHostAddress() : "Unknown";
    }

    public static String getLocalIpv6() {
        InetAddress address = getLocalAddress(true);
        return address != null ? address.getHostAddress() : "Unknown";
    }

    public static InetAddress getLocalAddress(boolean preferIPv6) {
        List<InetAddress> addresses = new ArrayList<>();
        Set<String> virtualNetworkInterfaces = new HashSet<>(Arrays.asList("vmware", "virtual", "hyper-v", "vbox", "virtualbox"));

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                String displayName = networkInterface.getDisplayName().toLowerCase();
                if (isVirtualInterface(displayName, virtualNetworkInterfaces)) {
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
        } catch (SocketException ex) {
            System.out.println("[EasyLAN] Error getting local IP address: " + ex.getMessage());
        }

        InetAddress ipv4Address = null;
        InetAddress ipv6Address = null;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                if (ipv4Address == null && isPrivateIPv4Address(address)) {
                    ipv4Address = address;
                }
            } else if (address instanceof Inet6Address) {
                if (ipv6Address == null && isValidIPv6Address(address)) {
                    ipv6Address = address;
                }
            }
        }

        return preferIPv6 ? ipv6Address : ipv4Address;
    }

    private static boolean isVirtualInterface(String displayName, Set<String> virtualNetworkInterfaces) {
        for (String virtualInterface : virtualNetworkInterfaces) {
            if (displayName.contains(virtualInterface)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPrivateIPv4Address(InetAddress address) {
        if (address instanceof Inet4Address) {
            String ip = address.getHostAddress();
            return ip.startsWith("10.") ||
                    ip.startsWith("192.168.") ||
                    ip.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
        }
        return false;
    }

    private static boolean isValidIPv6Address(InetAddress address) {
        if (address instanceof Inet6Address) {
            String ip = address.getHostAddress();
            return !ip.contains("%") && !ip.matches(".*:0:0:0:0:0:.*") && !ip.matches(".*::.*");
        }
        return false;
    }
}
