package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;
import org.xiaoxian.lan.ServerStarting;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;

@Mod(
        modid = EasyLAN.MOD_ID,
        name = EasyLAN.MOD_NAME,
        version = EasyLAN.VERSION,
        acceptableRemoteVersions = "*",
        guiFactory = "org.xiaoxian.HookConfigFactory"
)

public class EasyLAN {

    public static final String MOD_ID = "easylan";
    public static final String MOD_NAME = "EasyLAN";
    public static final String VERSION = "1.5";

    @Mod.Instance(MOD_ID)
    public static EasyLAN INSTANCE;

    // 如果您在开发环境中使用 runClient 进行测试，请修改此值为true
    // If you are using runClient for testing in your development environment, change this value to true
    public static boolean devMode = false;

    public static boolean allowPVP = true;
    public static boolean onlineMode = true;
    public static boolean spawnAnimals = true;
    public static boolean spawnNPCs = true;
    public static boolean allowFlight = true;
    public static boolean whiteList = false;
    public static boolean BanCommand = false;
    public static boolean OpCommand = false;
    public static boolean SaveCommand = false;
    public static boolean HttpAPI = true;
    public static boolean LanOutput = true;
    public static String CustomPort = "25565";
    public static String CustomMaxPlayer = "20";
    public static String motd = "This is a Default EasyLAN Motd!";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigUtil.load();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());
        MinecraftForge.EVENT_BUS.register(new ServerStarting());
        MinecraftForge.EVENT_BUS.register(new ShareToLan());

        GuiShareToLanEdit.PortText = CustomPort;
        GuiShareToLanEdit.MaxPlayerText = CustomMaxPlayer;
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        new ServerStarting().onServerStarting(event);
    }
}