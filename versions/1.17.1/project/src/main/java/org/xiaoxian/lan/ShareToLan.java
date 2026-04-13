package org.xiaoxian.lan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.easylan.forge.version.VersionBridgeResolver;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.util.ChatUtil;
import org.xiaoxian.util.NetworkUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.xiaoxian.EasyLAN.HttpAPI;
import static org.xiaoxian.EasyLAN.LanOutput;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;

public class ShareToLan {
    public static List<ServerPlayer> playerList;
    public static ExecutorService executorService;
    public static ScheduledExecutorService updateService;

    private final ApiLanStatus httpApi = new ApiLanStatus();
    private Integer httpApiPort;

    public void handleStop() {
        if (EasyLAN.getRuntimeState().isShared()) {
            EasyLAN.getRuntimeState().shutdownAll();
        }
    }

    public void handleLanSetup() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            return;
        }

        executorService = EasyLAN.getRuntimeState().openExecutorService(2);
        ServerConnectionListener connection = server.getConnection();

        if (!GuiShareToLanEdit.PortTextBox.getValue().isEmpty()) {
            startLanPort(connection, Integer.parseInt(GuiShareToLanEdit.PortTextBox.getValue()));
        }

        if (!GuiShareToLanEdit.MaxPlayerBox.getValue().isEmpty()) {
            setMaxPlayer(server);
        }

        if (HttpAPI) {
            updateService = EasyLAN.getRuntimeState().openUpdateService();
            startHttpApi(server);
        }

        if (LanOutput) {
            sendLanInfo(server);
        }

        EasyLAN.getRuntimeState().setShared(true);
    }

    private void sendLanInfo(IntegratedServer server) {
        executorService.submit(() -> {
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

            if (!GuiShareToLanEdit.PortTextBox.getValue().isEmpty()) {
                ChatUtil.sendMsg("&e" + I18n.get("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortTextBox.getValue());
            }

            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.get("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
            ChatUtil.sendMsg("&e" + I18n.get("easylan.text.onlineMode") + ": &a" + onlineMode);

            if (HttpAPI) {
                ChatUtil.sendMsg(" ");
                ChatUtil.sendMsg("&eHttp-Api:&a true");
                ChatUtil.sendMsg("&eStatus:&a localhost:" + safeValue(httpApiPort) + "/status");
                ChatUtil.sendMsg("&ePlayerList:&a localhost:" + safeValue(httpApiPort) + "/playerlist");
            }
            ChatUtil.sendMsg("&4---------------------");
        });
    }

    private void startHttpApi(IntegratedServer server) {
        executorService.submit(() -> {
            updateApiInfo(server);

            try {
                httpApiPort = httpApi.start();
            } catch (IOException ex) {
                System.out.println("[EasyLAN] HttpApi Start Error: " + ex.getMessage());
            }
        });
    }

    private void updateApiInfo(IntegratedServer server) {
        updateService.scheduleAtFixedRate(() -> {
            String resolvedPort = GuiShareToLanEdit.PortTextBox.getValue().isEmpty()
                    ? getLanPort(server)
                    : GuiShareToLanEdit.PortTextBox.getValue();

            httpApi.set("port", safeValue(resolvedPort));
            httpApi.set("version", safeValue(server.getServerVersion()));
            httpApi.set("owner", safeValue(server.getSingleplayerName()));
            httpApi.set("motd", safeValue(server.getMotd()));
            httpApi.set("pvp", String.valueOf(allowPVP));
            httpApi.set("onlineMode", String.valueOf(onlineMode));
            httpApi.set("spawnAnimals", String.valueOf(spawnAnimals));
            httpApi.set("spawnNPCs", String.valueOf(spawnNPCs));
            httpApi.set("allowFlight", String.valueOf(allowFlight));
            httpApi.set("difficulty", String.valueOf(server.getWorldData().getDifficulty()));
            httpApi.set("gameType", String.valueOf(server.getDefaultGameType()));
            httpApi.set("maxPlayer", String.valueOf(server.getMaxPlayers()));
            httpApi.set("onlinePlayer", String.valueOf(server.getPlayerCount()));

            playerList = server.getPlayerList().getPlayers();
            List<String> playerIds = new ArrayList<String>();
            for (ServerPlayer player : playerList) {
                playerIds.add(player.getDisplayName().getString());
            }
            ApiLanStatus.playerIDs = playerIds;
            EasyLAN.getRuntimeState().getStatusSnapshot().replacePlayers(playerIds);
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void setMaxPlayer(IntegratedServer server) {
        boolean success = VersionBridgeResolver.get().setMaxPlayers(server, Integer.parseInt(GuiShareToLanEdit.MaxPlayerBox.getValue()));
        if (!success) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.get("easylan.chat.CtPlayerError"));
            return;
        }

        if (!LanOutput) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.get("easylan.chat.CtPlayer") + " &f[&e" + GuiShareToLanEdit.MaxPlayerBox.getValue() + "&f]");
        }
    }

    private void startLanPort(ServerConnectionListener connection, int port) {
        try {
            VersionBridgeResolver.get().openLanEndpoint(connection, port);
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.get("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortTextBox.getValue() + "&f]");
            }
        } catch (IOException ex) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.get("easylan.chat.CtPortError"));
            System.out.println("[EasyLAN] addLanEndpoint Error: " + ex.getMessage());
        }
    }

    public static String getLanPort() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            return EasyLAN.getRuntimeState().getLanPort();
        }
        return getLanPort(server);
    }

    private static String getLanPort(IntegratedServer server) {
        return VersionBridgeResolver.get().resolveLanPort(server);
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
