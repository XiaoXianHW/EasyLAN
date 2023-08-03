package org.xiaoxian;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
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
    public static final String MOD_NAME = "EasyLAN";
    public static final String VERSION = "v1.2";

    @Mod.Instance(MOD_ID)
    public static EasyLan INSTANCE;
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


