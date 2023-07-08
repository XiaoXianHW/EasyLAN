package org.xiaoxian.lan;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xiaoxian.lan.ShareToLan.playerList;

public class ApiLanStatus {

    public static HttpServer server2;
    private static final Map<String, String> data = new HashMap<>();
    public static List<String> playerIDs = new ArrayList<>();

    public synchronized void start() throws IOException {
        if (server2 != null) {
            throw new IllegalStateException("HttpServer already started");
        }
        server2 = HttpServer.create(new InetSocketAddress(28960), 0);
        server2.createContext("/status", new StatusHandler());
        server2.createContext("/playerlist", new PlayerListHandler());
        server2.setExecutor(null);
        server2.start();
        System.out.println(server2);
    }

    public synchronized void stop() {
        System.out.println(server2);
        if (server2 == null) {
            throw new IllegalStateException("Server not running");
        }
        server2.stop(0);
        server2 = null;
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (Map.Entry<String, String> entry : data.entrySet()) {
                sb.append("\"");
                sb.append(entry.getKey());
                sb.append("\": \"");
                sb.append(entry.getValue());
                sb.append("\", ");
            }

            if (!data.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append(" }");

            String response = sb.toString();
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class PlayerListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            playerIDs.clear();
            for (EntityPlayerMP player : playerList) {
                playerIDs.add(player.getDisplayName());
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            for (String playerID : playerIDs) {
                sb.append("\"");
                sb.append(playerID);
                sb.append("\", ");
            }

            if (!playerIDs.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append(" ]");

            String response = sb.toString();

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

