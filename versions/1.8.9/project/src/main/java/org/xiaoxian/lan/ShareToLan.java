package org.xiaoxian.lan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.util.ChatUtil;
import org.xiaoxian.util.NetworkUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xiaoxian.EasyLAN.*;

public class ShareToLan {
    public static List<EntityPlayerMP> playerList;
    public static ExecutorService executorService;
    public static ScheduledExecutorService updateService;

    ApiLanStatus HttpApi = new ApiLanStatus();
    Integer HttpApiPort;
    static boolean isShared = false;

    @SubscribeEvent
    public void onGuiButtonClick(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.gui instanceof GuiShareToLanEdit.GuiShareToLanModified && event.button.id == 101) {
            executorService = Executors.newFixedThreadPool(3);
            handleLanSetup();
        }

        /* 关闭HttpAPI线程 */
        if (event.gui instanceof GuiIngameMenu && event.button.id == 1 && isShared) {
            executorService.shutdownNow();
            if (HttpAPI) {
                updateService.shutdownNow();
                HttpApi.stop();
            }
        }
    }

    private void handleLanSetup() {
        /* 变量区~ */
        String fieldName = devMode ? "maxPlayers" : "field_72405_c";
        Minecraft mc = Minecraft.getMinecraft();
        IntegratedServer server = mc.getIntegratedServer();
        NetworkSystem networkSystem = MinecraftServer.getServer().getNetworkSystem();

        /* 判断是否自定义端口号 */
        if (!(GuiShareToLanEdit.PortTextBox.getText().isEmpty())) {
            startLanPort(networkSystem, Integer.parseInt(GuiShareToLanEdit.PortTextBox.getText()));
        }

        /* 判断是否自定义最大玩家数 */
        if (!(GuiShareToLanEdit.MaxPlayerBox.getText().isEmpty())) {
            setMaxPlayer(fieldName);
        }

        /* 处理HttpAPI */
        if (HttpAPI) {
            updateService = Executors.newSingleThreadScheduledExecutor();
            startHttpApi(server);
        }

        /* 因为输出包含原版端口号，而只能在开放后进行获取，创建一个异步线程附加等待处理 */
        if (LanOutput) {
            sendLanInfo(server);
        }

        isShared = true;
    }

    private void sendLanInfo(IntegratedServer server) {
        executorService.submit(() -> {
            String lanIPv4 = NetworkUtil.getLocalIpv4();
            String lanIPv6 = NetworkUtil.getLocalIpv6();
            String publicIPv4 = NetworkUtil.getPublicIPv4();
            boolean checkIPv4 = NetworkUtil.checkIpIsPublic();
            String lanPort = getLanPort();

            ChatUtil.sendMsg("&e[&6EasyLan&e] &aSuccessfully");
            ChatUtil.sendMsg("&4---------------------");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv4: &a" + lanIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv6: &a" + lanIPv6);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.public") + "IPv4: &a" + publicIPv4);
            ChatUtil.sendMsg("&e" + I18n.format("easylan.chat.isPublic") + ": &a" + checkIPv4);
            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.port") + ": &a" + lanPort);

            if (!(GuiShareToLanEdit.PortTextBox.getText().isEmpty())) {
                ChatUtil.sendMsg("&e" + I18n.format("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortTextBox.getText());
            }

            ChatUtil.sendMsg(" ");
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.onlineMode") + ": &a" + onlineMode);

            if (HttpAPI) {
                ChatUtil.sendMsg(" ");
                ChatUtil.sendMsg("&eHttp-Api:&a true");
                ChatUtil.sendMsg("&eStatus:&a localhost:" + HttpApiPort + "/status");
                ChatUtil.sendMsg("&ePlayerList:&a localhost:" + HttpApiPort + "/playerlist");
            }
            ChatUtil.sendMsg("&4---------------------");
        });
    }

    private void startHttpApi(IntegratedServer server) {
        executorService.submit(() -> {
            System.out.println("[EasyLAN] Starting Thread!");

            updateApiInfo(server);

            try {
                HttpApiPort = HttpApi.start();
            } catch (IOException e) {
                System.out.println("[EasyLAN] HttpApi Start Error!" + e.getMessage());
            }
        });
    }

    /* 定时异步处理API */
    private void updateApiInfo(IntegratedServer server) {
        updateService.scheduleAtFixedRate(() -> {
            if (GuiShareToLanEdit.PortTextBox.getText().isEmpty()) {
                HttpApi.set("port", String.valueOf(getLanPort()));
            } else {
                HttpApi.set("port", GuiShareToLanEdit.PortTextBox.getText());
            }

            HttpApi.set("version", server.getMinecraftVersion());
            HttpApi.set("owner", server.getServerOwner());
            HttpApi.set("motd", server.getMOTD());
            HttpApi.set("pvp", String.valueOf(allowPVP));
            HttpApi.set("onlineMode", String.valueOf(onlineMode));
            HttpApi.set("spawnAnimals", String.valueOf(spawnAnimals));
            HttpApi.set("spawnNPCs", String.valueOf(spawnNPCs));
            HttpApi.set("allowFlight", String.valueOf(allowFlight));
            HttpApi.set("difficulty", String.valueOf(server.getDifficulty()));
            HttpApi.set("gameType", String.valueOf(server.getGameType()));
            HttpApi.set("maxPlayer", String.valueOf(server.getMaxPlayers()));
            HttpApi.set("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));

            playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
            List<String> playerIDs = new ArrayList<>();
            for (EntityPlayerMP player : playerList) {
                playerIDs.add(player.getName());
            }
            ApiLanStatus.playerIDs = playerIDs;

        }, 100, 500, TimeUnit.MILLISECONDS);
    }

    private void setMaxPlayer(String fieldName) {
        try {
            ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();
            Class<?> minecraftServerPlayerClass = Class.forName("net.minecraft.server.management.ServerConfigurationManager");
            Field maxplayerField = minecraftServerPlayerClass.getDeclaredField(fieldName);
            maxplayerField.setAccessible(true);
            maxplayerField.set(configManager, Integer.parseInt(GuiShareToLanEdit.MaxPlayerBox.getText()));
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPlayer") + " &f[&e" + GuiShareToLanEdit.MaxPlayerBox.getText() + "&f]");
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            ChatUtil.sendMsg("&e[&6EasyLan&e] &c" + I18n.format("easylan.chat.CtPlayerError"));
            System.out.println("[EasyLAN] setMaxPlayer Error: " + e.getMessage());
        }
    }

    private void startLanPort(NetworkSystem networkSystem, int port) {
        try {
            networkSystem.addLanEndpoint(InetAddress.getByName("0.0.0.0"), port);
            if (!LanOutput) {
                ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortTextBox.getText() + "&f]");
            }
        } catch (IOException e) {
            ChatUtil.sendMsg("&e[&6EasyLan&e] &c" + I18n.format("easylan.chat.CtPortError"));
            System.out.println("[EasyLan] addLanEndpoint Error: " + e.getMessage());
        }
    }

    public static String getLanPort() {
        String lastPort = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("logs/latest.log"))) {
            String line;
            Pattern pattern = Pattern.compile("Started on ([0-9]*)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    lastPort = matcher.group(1);
                }
            }
        } catch (IOException e) {
            System.out.println("[EasyLan] getLanPort Error: " + e.getMessage());
            return null;
        }
        return lastPort;
    }
}