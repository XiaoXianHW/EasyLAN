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
        modid = EasyLan.MOD_ID,
        name = EasyLan.MOD_NAME,
        version = EasyLan.VERSION,
        acceptableRemoteVersions = "*",
        guiFactory = "org.xiaoxian.HookConfigFactory"
)

public class EasyLan {

    public static final String MOD_ID = "easylan";
    public static final String MOD_NAME = "EasyLan";
    public static final String VERSION = "v1";

    @Mod.Instance(MOD_ID)
    public static EasyLan INSTANCE;

    // 如果处于IDEA等开发环境，请修改此值为true，否则可能无法正常运行
    // If you are in a development environment such as IDEA, please modify this boolean to true, otherwise it may not work properly
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
    public static String motd = "This is a Default EasyLAN Motd!";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigUtil.load();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());
        MinecraftForge.EVENT_BUS.register(new ServerStarting());
        MinecraftForge.EVENT_BUS.register(new ShareToLan());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        new ServerStarting().onServerStarting(event);
    }
}


