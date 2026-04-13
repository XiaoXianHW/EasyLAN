package org.xiaoxian.easylan.core.net;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import org.xiaoxian.easylan.core.model.EasyLanStatusSnapshot;

import java.io.IOException;
import java.net.ServerSocket;

public class LocalHttpApiServer {
    private static final Gson GSON = new Gson();

    private final EasyLanStatusSnapshot snapshot;
    private SimpleHttpServer server;
    private Integer port;

    public LocalHttpApiServer(EasyLanStatusSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public synchronized int start() throws IOException {
        if (server != null && port != null) {
            return port;
        }

        int targetPort = 28960;
        while (isPortInUse(targetPort)) {
            targetPort++;
        }

        server = new SimpleHttpServer(targetPort, snapshot);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        port = targetPort;
        return targetPort;
    }

    public synchronized void stop() {
        if (server == null) {
            return;
        }

        server.stop();
        server = null;
        port = null;
    }

    public synchronized Integer getPort() {
        return port;
    }

    private boolean isPortInUse(int targetPort) {
        try (ServerSocket ignored = new ServerSocket(targetPort)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static class SimpleHttpServer extends NanoHTTPD {
        private final EasyLanStatusSnapshot snapshot;

        private SimpleHttpServer(int port, EasyLanStatusSnapshot snapshot) {
            super(port);
            this.snapshot = snapshot;
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            if ("/status".equals(uri)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", GSON.toJson(snapshot.copyStatus()));
            }

            if ("/playerlist".equals(uri)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", GSON.toJson(snapshot.copyPlayers()));
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "Not Found");
        }
    }
}
