package org.xiaoxian.lan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xiaoxian.EasyLan.*;
import static org.xiaoxian.lan.ApiLanStatus.server2;

public class ShareToLan {
    ApiLanStatus HttpApi = new ApiLanStatus();
    public static List<EntityPlayerMP> playerList;

    @SubscribeEvent
    public void onGuiButtonClick(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.getGui() instanceof GuiShareToLanEdit.GuiShareToLanModified) {
            if (event.getButton().id == 101) {

                /* 变量区~ */
                String fieldName = devMode ? "maxPlayers" : "field_72405_c";
                Minecraft mc = Minecraft.getMinecraft();
                IntegratedServer server = mc.getIntegratedServer();
                assert server != null;
                NetworkSystem networkSystem = server.getNetworkSystem();

                /* 判断是否自定义端口号 */
                if (!(GuiShareToLanEdit.PortTextBox.getText().isEmpty())) {
                    try {
                        networkSystem.addLanEndpoint(InetAddress.getByName("0.0.0.0"), Integer.parseInt(GuiShareToLanEdit.PortTextBox.getText()));
                        if (!LanOutput) {
                            ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPort") + " &f[&e" + GuiShareToLanEdit.PortTextBox.getText() + "&f]");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                /* 判断是否自定义最大玩家数 */
                if (!(GuiShareToLanEdit.MaxPlayerBox.getText().isEmpty())) {
                    try {
                        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
                        Class<?> minecraftServerPlayerClass = Class.forName("net.minecraft.server.management.PlayerList");
                        Field maxplayerField = minecraftServerPlayerClass.getDeclaredField(fieldName);
                        maxplayerField.setAccessible(true);
                        maxplayerField.set(playerList, Integer.parseInt(GuiShareToLanEdit.MaxPlayerBox.getText()));
                        if (!LanOutput) {
                            ChatUtil.sendMsg("&e[&6EasyLan&e] &a" + I18n.format("easylan.chat.CtPlayer") + " &f[&e" + GuiShareToLanEdit.MaxPlayerBox.getText() + "&f]");
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
                        if (GuiShareToLanEdit.PortTextBox.getText().isEmpty()) {
                            HttpApi.set("port", getLanPort());
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
                        playerList = server.getPlayerList().getPlayers();
                        List<String> playerIDs = new ArrayList<>();
                        for (EntityPlayerMP player : playerList) {
                            playerIDs.add(player.getName());
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
                    HttpApi.set("difficulty", String.valueOf(server.getDifficulty()));
                    HttpApi.set("onlinePlayer", String.valueOf(server.getCurrentPlayerCount()));

                    playerList = server.getPlayerList().getPlayers();
                    List<String> playerIDs = new ArrayList<>();
                    for (EntityPlayerMP player : playerList) {
                        playerIDs.add(player.getName());
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
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.local") + "IPv4: &a" + LocalIPv4);
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.public") + "IPv4: &a" + PublicIPv4);
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.chat.isPublic") + ": &a" + isPublic);
                        ChatUtil.sendMsg(" ");
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.text.port") + ": &a" + getLanPort());
                        if (!(GuiShareToLanEdit.PortTextBox.getText().isEmpty())) {
                            ChatUtil.sendMsg("&e" + I18n.format("easylan.text.CtPort") + ": &a" + GuiShareToLanEdit.PortTextBox.getText());
                        }
                        ChatUtil.sendMsg(" ");
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.text.maxplayer") + ": &a" + server.getMaxPlayers());
                        ChatUtil.sendMsg("&e" + I18n.format("easylan.text.onlineMode") + ": &a" + onlineMode);
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
        }

        /* 关闭HttpAPI线程 */
        if (event.getGui() instanceof GuiIngameMenu) {
            if (event.getButton().id == 1) {
                if (HttpAPI) {
                     if (!(server2 == null)) {
                         HttpApi.stop();
                         System.out.println("HttpApi Stopped!");
                     }
                }
            }
        }
    }

    public String getLanPort() {
        try (BufferedReader reader = new BufferedReader(new FileReader("logs/latest.log"))) {
            String line;
            Pattern pattern = Pattern.compile("Started on ([0-9]*)");
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
