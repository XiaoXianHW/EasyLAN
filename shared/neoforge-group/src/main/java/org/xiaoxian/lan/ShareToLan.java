package org.xiaoxian.lan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.easylan.core.model.EasyLanStatusSnapshot;
import org.xiaoxian.easylan.neoforge.version.VersionBridgeResolver;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.util.ChatUtil;
import org.xiaoxian.util.NetworkUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.xiaoxian.EasyLAN.HttpAPI;
import static org.xiaoxian.EasyLAN.LanOutput;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;

public class ShareToLan {
    public void handleStop() {
        EasyLAN.getRuntimeState().shutdownAll();
    }

    public void handleLanSetup() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }

        ServerConnectionListener connection = server.getConnection();
        String customPort = GuiShareToLanEdit.PortText;
        String customMaxPlayer = GuiShareToLanEdit.MaxPlayerText;

        EasyLAN.getRuntimeState().openExecutorService(2);

        if (!isBlank(customPort)) {
            startLanPort(connection, Integer.parseInt(customPort));
        }

        if (!isBlank(customMaxPlayer)) {
            setMaxPlayer(server, Integer.parseInt(customMaxPlayer));
        }

        if (HttpAPI) {
            EasyLAN.getRuntimeState().openUpdateService();
            startHttpApi(server);
        }

        if (LanOutput) {
            sendLanInfo(server);
        }

        EasyLAN.getRuntimeState().setShared(true);
    }

    private void sendLanInfo(final IntegratedServer server) {
        EasyLAN.getRuntimeState().openExecutorService(2).submit(() -> {
            String lanIPv4 = NetworkUtil.getLocalIpv4();
            String lanIPv6 = NetworkUtil.getLocalIpv6();
            String publicIPv4 = NetworkUtil.getPublicIPv4();
            boolean publicReachable = NetworkUtil.checkIpIsPublic();
            String lanPort = getLanPort(server);

            ChatUtil.sendMsg("&e[&6EasyLAN&e] &aSuccessfully");
            ChatUtil.sendMsg("&4---------------------");
            ChatUtil.sendMsg("&e" + I18n.get("easylan.local") + "IPv4: &a" + lanIPv4);
            ChatUtil.sendMsg("&e" + I18n.get("easylan.local") + "IPv6: &a" + lanIPv6);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.get("easylan.public") + "IPv4: &a" + publicIPv4);
            ChatUtil.sendMsg("&e" + I18n.get("easylan.chat.isPublic") + ": &a" + publicReachable);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.get("easylan.text.port") + ": &a" + safeValue(lanPort));

            if (!isBlank(GuiShareToLanEdit.PortText)) {
                ChatUtil.sendMsg("&e" + I18n.get("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortText);
            }

            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.get("easylan.text.maxplayer") + ": &a" + resolveMaxPlayers(server));
            ChatUtil.sendMsg("&e" + I18n.get("easylan.text.onlineMode") + ": &a" + onlineMode);

            if (HttpAPI) {
                ChatUtil.sendMsg(" ");
                ChatUtil.sendMsg("&eHttp-Api:&a true");
                ChatUtil.sendMsg("&eStatus:&a localhost:" + safeValue(EasyLAN.getRuntimeState().getHttpApiPort()) + "/status");
                ChatUtil.sendMsg("&ePlayerList:&a localhost:" + safeValue(EasyLAN.getRuntimeState().getHttpApiPort()) + "/playerlist");
            }
            ChatUtil.sendMsg("&4---------------------");
        });
    }

    private void startHttpApi(final IntegratedServer server) {
        EasyLAN.getRuntimeState().openExecutorService(2).submit(() -> {
            updateApiInfo(server);

            try {
                EasyLAN.getRuntimeState().startHttpApi();
            } catch (IOException ex) {
                System.out.println("[EasyLAN] HttpApi Start Error: " + ex.getMessage());
            }
        });
    }

    private void updateApiInfo(final IntegratedServer server) {
        EasyLanStatusSnapshot snapshot = EasyLAN.getRuntimeState().getStatusSnapshot();
        EasyLAN.getRuntimeState().openUpdateService().scheduleAtFixedRate(() -> {
            String resolvedPort = isBlank(GuiShareToLanEdit.PortText) ? getLanPort(server) : GuiShareToLanEdit.PortText;

            snapshot.putStatus("port", safeValue(resolvedPort));
            snapshot.putStatus("version", safeValue(server.getServerVersion()));
            snapshot.putStatus("owner", safeValue(server.getSingleplayerProfile().getName()));
            snapshot.putStatus("motd", safeValue(server.getMotd()));
            snapshot.putStatus("pvp", String.valueOf(allowPVP));
            snapshot.putStatus("onlineMode", String.valueOf(onlineMode));
            snapshot.putStatus("spawnAnimals", String.valueOf(spawnAnimals));
            snapshot.putStatus("allowFlight", String.valueOf(allowFlight));
            snapshot.putStatus("difficulty", safeValue(server.getWorldData().getDifficulty()));
            snapshot.putStatus("gameType", safeValue(server.getDefaultGameType()));
            snapshot.putStatus("maxPlayer", String.valueOf(resolveMaxPlayers(server)));
            snapshot.putStatus("onlinePlayer", String.valueOf(server.getPlayerCount()));

            List<String> playerIds = new ArrayList<>();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                playerIds.add(player.getDisplayName().getString());
            }
            snapshot.replacePlayers(playerIds);
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void setMaxPlayer(IntegratedServer server, int maxPlayers) {
        boolean success = VersionBridgeResolver.get().setMaxPlayers(server, maxPlayers);
        if (!success) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.get("easylan.chat.CtPlayerError"));
            return;
        }

        if (!LanOutput) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.get("easylan.chat.CtPlayer") + " &f[&e" + maxPlayers + "&f]");
        }
    }

    private void startLanPort(ServerConnectionListener connection, int port) {
        try {
            VersionBridgeResolver.get().openLanEndpoint(connection, port);
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.get("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortText + "&f]");
            }
        } catch (IOException ex) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.get("easylan.chat.CtPortError"));
            System.out.println("[EasyLAN] addLanEndpoint Error: " + ex.getMessage());
        }
    }

    public static String getLanPort() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return EasyLAN.getRuntimeState().getLanPort();
        }
        return getLanPort(server);
    }

    private static String getLanPort(IntegratedServer server) {
        return VersionBridgeResolver.get().resolveLanPort(server);
    }

    private static int resolveMaxPlayers(IntegratedServer server) {
        int resolved = VersionBridgeResolver.get().resolveMaxPlayers(server);
        return resolved > 0 ? resolved : server.getMaxPlayers();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }

    private static String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
