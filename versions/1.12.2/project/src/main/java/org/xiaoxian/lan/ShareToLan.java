package org.xiaoxian.lan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.integrated.IntegratedServer;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.easylan.core.model.EasyLanStatusSnapshot;
import org.xiaoxian.easylan.forge.version.VersionBridgeResolver;
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
import static org.xiaoxian.EasyLAN.spawnNPCs;

public class ShareToLan {
    public void handleStop() {
        if (EasyLAN.getRuntimeState().isShared()) {
            EasyLAN.getRuntimeState().shutdownAll();
        }
    }

    public void handleLanSetup() {
        Minecraft minecraft = Minecraft.getMinecraft();
        IntegratedServer server = minecraft.getIntegratedServer();
        if (server == null) {
            return;
        }

        NetworkSystem networkSystem = server.getNetworkSystem();
        String customPort = GuiShareToLanEdit.PortText;
        String customMaxPlayer = GuiShareToLanEdit.MaxPlayerText;

        EasyLAN.getRuntimeState().openExecutorService(2);

        if (customPort != null && !customPort.isEmpty()) {
            startLanPort(networkSystem, Integer.parseInt(customPort));
        }

        if (customMaxPlayer != null && !customMaxPlayer.isEmpty()) {
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
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv4: &a" + lanIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv6: &a" + lanIPv6);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.public") + "IPv4: &a" + publicIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.chat.isPublic") + ": &a" + publicReachable);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.port") + ": &a" + safeValue(lanPort));

            if (GuiShareToLanEdit.PortText != null && !GuiShareToLanEdit.PortText.isEmpty()) {
                ChatUtil.sendMsg("&e" + I18n.format("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortText);
            }

            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.onlineMode") + ": &a" + onlineMode);

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
            String resolvedPort = GuiShareToLanEdit.PortText == null || GuiShareToLanEdit.PortText.isEmpty()
                    ? getLanPort(server)
                    : GuiShareToLanEdit.PortText;

            snapshot.putStatus("port", safeValue(resolvedPort));
            snapshot.putStatus("version", safeValue(server.getMinecraftVersion()));
            snapshot.putStatus("owner", safeValue(server.getServerOwner()));
            snapshot.putStatus("motd", safeValue(server.getMOTD()));
            snapshot.putStatus("pvp", String.valueOf(allowPVP));
            snapshot.putStatus("onlineMode", String.valueOf(onlineMode));
            snapshot.putStatus("spawnAnimals", String.valueOf(spawnAnimals));
            snapshot.putStatus("spawnNPCs", String.valueOf(spawnNPCs));
            snapshot.putStatus("allowFlight", String.valueOf(allowFlight));
            snapshot.putStatus("difficulty", String.valueOf(server.getDifficulty()));
            snapshot.putStatus("gameType", String.valueOf(server.getGameType()));
            snapshot.putStatus("maxPlayer", String.valueOf(server.getMaxPlayers()));
            snapshot.putStatus("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));

            List<String> playerIds = new ArrayList<>();
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                playerIds.add(player.getName());
            }
            snapshot.replacePlayers(playerIds);
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void setMaxPlayer(IntegratedServer server, int maxPlayers) {
        boolean success = VersionBridgeResolver.get().setMaxPlayers(server, maxPlayers);
        if (!success) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.format("easylan.chat.CtPlayerError"));
            return;
        }

        if (!LanOutput) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.format("easylan.chat.CtPlayer") + " &f[&e" + maxPlayers + "&f]");
        }
    }

    private void startLanPort(NetworkSystem networkSystem, int port) {
        try {
            VersionBridgeResolver.get().openLanEndpoint(networkSystem, port);
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLAN&e] &a" + I18n.format("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortText + "&f]");
            }
        } catch (IOException ex) {
            ChatUtil.sendMsg("&e[&6EasyLAN&e] &c" + I18n.format("easylan.chat.CtPortError"));
            System.out.println("[EasyLAN] addLanEndpoint Error: " + ex.getMessage());
        }
    }

    public static String getLanPort() {
        Minecraft minecraft = Minecraft.getMinecraft();
        IntegratedServer server = minecraft.getIntegratedServer();
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
