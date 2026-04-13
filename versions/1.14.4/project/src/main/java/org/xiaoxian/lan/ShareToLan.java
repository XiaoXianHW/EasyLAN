package org.xiaoxian.lan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.integrated.IntegratedServer;
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
    public static List<ServerPlayerEntity> playerList;
    public static ExecutorService executorService;
    public static ScheduledExecutorService updateService;

    private static final ApiLanStatus HTTP_API_SERVER = new ApiLanStatus();
    private static Integer httpApiPort;

    public void handleStop() {
        if (EasyLAN.getRuntimeState().isShared()) {
            EasyLAN.getRuntimeState().shutdownAll();
        }
    }

    public void handleLanSetup() {
        executorService = EasyLAN.getRuntimeState().openExecutorService(2);

        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null) {
            return;
        }

        NetworkSystem networkSystem = server.getNetworkSystem();

        if (!GuiShareToLanEdit.PortTextBox.getText().isEmpty()) {
            startLanPort(networkSystem, Integer.parseInt(GuiShareToLanEdit.PortTextBox.getText()));
        }

        if (!GuiShareToLanEdit.MaxPlayerBox.getText().isEmpty()) {
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
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv4: &a" + lanIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv6: &a" + lanIPv6);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.public") + "IPv4: &a" + publicIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.chat.isPublic") + ": &a" + publicReachable);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.port") + ": &a" + safeValue(lanPort));

            if (!GuiShareToLanEdit.PortTextBox.getText().isEmpty()) {
                ChatUtil.sendMsg("&e" + I18n.format("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortTextBox.getText());
            }

            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.onlineMode") + ": &a" + onlineMode);

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
                httpApiPort = HTTP_API_SERVER.start();
            } catch (IOException ex) {
                System.out.println("[EasyLAN] HttpApi Start Error: " + ex.getMessage());
            }
        });
    }

    private void updateApiInfo(IntegratedServer server) {
        updateService.scheduleAtFixedRate(() -> {
            String resolvedPort = GuiShareToLanEdit.PortTextBox.getText().isEmpty()
                    ? getLanPort(server)
                    : GuiShareToLanEdit.PortTextBox.getText();

            HTTP_API_SERVER.set("port", safeValue(resolvedPort));
            HTTP_API_SERVER.set("version", safeValue(server.getMinecraftVersion()));
            HTTP_API_SERVER.set("owner", safeValue(server.getServerOwner()));
            HTTP_API_SERVER.set("motd", safeValue(server.getMOTD()));
            HTTP_API_SERVER.set("pvp", String.valueOf(allowPVP));
            HTTP_API_SERVER.set("onlineMode", String.valueOf(onlineMode));
            HTTP_API_SERVER.set("spawnAnimals", String.valueOf(spawnAnimals));
            HTTP_API_SERVER.set("spawnNPCs", String.valueOf(spawnNPCs));
            HTTP_API_SERVER.set("allowFlight", String.valueOf(allowFlight));
            HTTP_API_SERVER.set("difficulty", String.valueOf(server.getDifficulty()));
            HTTP_API_SERVER.set("gameType", String.valueOf(server.getGameType()));
            HTTP_API_SERVER.set("maxPlayer", String.valueOf(server.getMaxPlayers()));
            HTTP_API_SERVER.set("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));

            playerList = server.getPlayerList().getPlayers();
            List<String> playerIds = new ArrayList<String>();
            for (ServerPlayerEntity player : playerList) {
                playerIds.add(player.getDisplayName().getString());
            }
            ApiLanStatus.playerIDs = playerIds;
            EasyLAN.getRuntimeState().getStatusSnapshot().replacePlayers(playerIds);
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void setMaxPlayer(IntegratedServer server) {
        boolean success = VersionBridgeResolver.get().setMaxPlayers(server, Integer.parseInt(GuiShareToLanEdit.MaxPlayerBox.getText()));
        if (!success) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.format("easylan.chat.CtPlayerError"));
            return;
        }

        if (!LanOutput) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.format("easylan.chat.CtPlayer") + " &f[&e" + GuiShareToLanEdit.MaxPlayerBox.getText() + "&f]");
        }
    }

    private void startLanPort(NetworkSystem connection, int port) {
        try {
            VersionBridgeResolver.get().openLanEndpoint(connection, port);
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.format("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortTextBox.getText() + "&f]");
            }
        } catch (IOException ex) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.format("easylan.chat.CtPortError"));
            System.out.println("[EasyLAN] addLanEndpoint Error: " + ex.getMessage());
        }
    }

    public static String getLanPort() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getIntegratedServer();
        if (server == null) {
            return EasyLAN.getRuntimeState().getLanPort();
        }
        return getLanPort(server);
    }

    private static String getLanPort(IntegratedServer server) {
        return VersionBridgeResolver.get().resolveLanPort(server);
    }

    private static String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
