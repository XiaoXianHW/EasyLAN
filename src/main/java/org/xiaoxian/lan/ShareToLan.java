package org.xiaoxian.lan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.util.ChatUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xiaoxian.EasyLAN.*;
import static org.xiaoxian.lan.ApiLanStatus.server2;

public class ShareToLan {
    static ApiLanStatus HttpApi = new ApiLanStatus();
    public static List<ServerPlayer> playerList;

    public static void NewShareToLAN() {
        /* 变量区~ */
        String fieldName = devMode ? "maxPlayers" : "f_11193_";
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        assert server != null;
        ServerConnectionListener networkSystem = server.getConnection();

        /* 判断是否自定义端口号 */
        if (!(GuiShareToLanEdit.PortTextBox.getValue().isEmpty())) {
            try {
                assert networkSystem != null;
                networkSystem.startTcpServerListener(InetAddress.getByName("0.0.0.0"), Integer.parseInt(GuiShareToLanEdit.PortTextBox.getValue()));
                if (!LanOutput) {
                    ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.get("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortTextBox.getValue() + "&f]");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* 判断是否自定义最大玩家数 */
        if (!(GuiShareToLanEdit.MaxPlayerBox.getValue().isEmpty())) {
            try {
                PlayerList playerList = new ServerStartingEvent(server).getServer().getPlayerList();
                Class<?> minecraftServerPlayerClass = Class.forName("net.minecraft.server.players.PlayerList");
                Field maxplayerField = minecraftServerPlayerClass.getDeclaredField(fieldName);
                maxplayerField.setAccessible(true);
                maxplayerField.set(playerList, Integer.parseInt(GuiShareToLanEdit.MaxPlayerBox.getValue()));
                if (!LanOutput) {
                    ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.get("easylan.chat.CtPlayer") + " &e" + GuiShareToLanEdit.MaxPlayerBox.getValue());
                }
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        /* 异步处理HttpAPI */
        if (HttpAPI) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                System.out.println("Start HttpApi Thread");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Starting Thread!");
                if (GuiShareToLanEdit.PortTextBox.getValue().isEmpty()) {
                    HttpApi.set("port", getLanPort());
                } else {
                    HttpApi.set("port", GuiShareToLanEdit.PortTextBox.getValue());
                }

                HttpApi.set("version", server.getServerVersion());
                HttpApi.set("owner", Objects.requireNonNull(server.getSingleplayerProfile()).getName());
                HttpApi.set("motd", server.getMotd());
                HttpApi.set("pvp", String.valueOf(allowPVP));
                HttpApi.set("onlineMode", String.valueOf(onlineMode));
                HttpApi.set("spawnAnimals", String.valueOf(spawnAnimals));
                HttpApi.set("allowFlight", String.valueOf(allowFlight));
                HttpApi.set("difficulty", String.valueOf(server.getWorldData().getDifficulty()));
                HttpApi.set("gameType", String.valueOf(server.getDefaultGameType()));
                HttpApi.set("maxPlayer", String.valueOf(server.getMaxPlayers()));
                HttpApi.set("onlinePlayer", String.valueOf(server.getPlayerCount()));
                playerList = server.getPlayerList().getPlayers();
                List<String> playerIDs = new ArrayList<>();
                for (ServerPlayer player : playerList) {
                    playerIDs.add(String.valueOf(player.getName()));
                }
                ApiLanStatus.playerIDs = playerIDs;

                try {
                    HttpApi.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        /* 定时异步处理API */
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            HttpApi.set("difficulty", String.valueOf(server.getWorldData().getDifficulty()));
            HttpApi.set("onlinePlayer", String.valueOf(server.getPlayerCount()));

            playerList = server.getPlayerList().getPlayers();
            List<String> playerIDs = new ArrayList<>();
            for (ServerPlayer player : playerList) {
                playerIDs.add(player.getName().getString());
            }
            ApiLanStatus.playerIDs = playerIDs;

        }, 100, 100, TimeUnit.MILLISECONDS);

        /* 因为输出包含原版端口号，而只能在开放后进行获取，创建一个异步线程附加等待处理 */
        if (LanOutput) {
            ExecutorService executor2 = Executors.newSingleThreadExecutor();
            executor2.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String isPublic;
                String PublicIPv4 = "null";
                String LocalIPv4 = "null";
                try {
                    URL url = new URL("https://api.axtn.net/api/myipcheck.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        isPublic = "Yes";
                    } else {
                        isPublic = "No";
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                    PublicIPv4 = jsonObject.get("ip").getAsString();

                } catch (Exception e) {
                    isPublic = "No";
                }

                try {
                    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (networkInterfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = networkInterfaces.nextElement();
                        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress inetAddress = inetAddresses.nextElement();
                            if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
                                LocalIPv4 = inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                ChatUtil.sendMsg("&e[&6EasyLan&e] &aSuccessfully");
                ChatUtil.sendMsg("&4---------------------");
                ChatUtil.sendMsg("&e" + I18n.get("easylan.local") + "IPv4: &a" + LocalIPv4);
                ChatUtil.sendMsg("&e" + I18n.get("easylan.public") + "IPv4: &a" + PublicIPv4);
                ChatUtil.sendMsg("&e" + I18n.get("easylan.chat.isPublic") + ": &a" + isPublic);
                ChatUtil.sendMsg(" ");
                ChatUtil.sendMsg("&e" + I18n.get("easylan.text.port") + ": &a" + getLanPort());
                if (!(GuiShareToLanEdit.PortTextBox.getValue().isEmpty())) {
                    ChatUtil.sendMsg("&e" + I18n.get("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortTextBox.getValue());
                }
                ChatUtil.sendMsg(" ");
                ChatUtil.sendMsg("&e" + I18n.get("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
                ChatUtil.sendMsg("&e" + I18n.get("easylan.text.onlineMode") + ": &a" + onlineMode);
                ChatUtil.sendMsg(" ");
                if (HttpAPI) {
                    ChatUtil.sendMsg("&eHttp-Api:&a true");
                    ChatUtil.sendMsg("&eApi-Status:&a localhost:28960/status");
                    ChatUtil.sendMsg("&eApi-PlayerList:&a localhost:28960/playerlist");
                }
                ChatUtil.sendMsg("&4---------------------");
            });
        }
    }
    /* 关闭HttpAPI线程 */
    public static void StopHttpAPIServer() {
        if (HttpAPI) {
            if (!(server2 == null)) {
                HttpApi.stop();
                System.out.println("HttpApi Stopped!");
            }
        }
    }

    public static String getLanPort() {
        try (BufferedReader reader = new BufferedReader(new FileReader("logs/latest.log"))) {
            String line;
            Pattern pattern = Pattern.compile("Started serving on ([0-9]*)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
