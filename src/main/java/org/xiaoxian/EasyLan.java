package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoxian.gui.GuiExitGame;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;
import org.xiaoxian.lan.ServerStarting;
import org.xiaoxian.util.ConfigUtil;

@Mod(EasyLAN.MOD_ID)
public class EasyLAN {

    public static final String MOD_ID = "easylan";

    // 如果处于IDEA等开发环境，请修改此值为true，否则可能无法正常运行
    // If you are in a development environment such as IDEA, please modify this boolean to true, otherwise it may not work properly
    public static boolean devMode = false;

    public static boolean allowPVP = true;
    public static boolean onlineMode = true;
    public static boolean spawnAnimals = true;
    public static boolean allowFlight = true;
    public static boolean whiteList = false;
    public static boolean BanCommands = false;
    public static boolean OpCommands = false;
    public static boolean SaveCommands = false;
    public static boolean HttpAPI = true;
    public static boolean LanOutput = true;
    public static String CustomPort = "25565";
    public static String CustomMaxPlayer = "20";
    public static String motd = "This is a Default EasyLAN Motd!";

    public EasyLAN() {
        ConfigUtil.load();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());
        MinecraftForge.EVENT_BUS.register(new ServerStarting());
        MinecraftForge.EVENT_BUS.register(new GuiExitGame());

        GuiShareToLanEdit.PortText = CustomPort;
        GuiShareToLanEdit.MaxPlayerText = CustomMaxPlayer;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        new ServerStarting().onServerStarting(event);
    }
}
