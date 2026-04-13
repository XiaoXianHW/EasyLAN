package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.xiaoxian.easylan.core.config.EasyLanConfig;
import org.xiaoxian.easylan.core.model.LanRuleProfile;
import org.xiaoxian.easylan.core.runtime.EasyLanRuntimeState;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;
import org.xiaoxian.lan.ServerStarting;
import org.xiaoxian.lan.ServerStopping;
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

    private static final EasyLanConfig CONFIG = EasyLanConfig.defaultConfig();
    private static final EasyLanRuntimeState RUNTIME_STATE = new EasyLanRuntimeState();

    @Mod.Instance(MOD_ID)
    public static EasyLAN INSTANCE;

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
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());

        GuiShareToLanEdit.PortText = CustomPort;
        GuiShareToLanEdit.MaxPlayerText = CustomMaxPlayer;
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        new ServerStarting().onServerStarting(event);
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        new ServerStopping().onServerStopping(event);
    }

    public static EasyLanConfig getConfig() {
        return CONFIG;
    }

    public static EasyLanRuntimeState getRuntimeState() {
        return RUNTIME_STATE;
    }

    public static void syncFromConfig() {
        LanRuleProfile rules = CONFIG.getRuleProfile();
        allowPVP = rules.isAllowPvp();
        onlineMode = rules.isOnlineMode();
        spawnAnimals = rules.isSpawnAnimals();
        spawnNPCs = rules.isSpawnNpcs();
        allowFlight = rules.isAllowFlight();
        whiteList = rules.isWhiteList();
        BanCommand = rules.isBanCommands();
        OpCommand = rules.isOpCommands();
        SaveCommand = rules.isSaveCommands();
        HttpAPI = rules.isHttpApi();
        LanOutput = rules.isLanOutput();
        motd = rules.getMotd();
        CustomPort = CONFIG.getCustomPort();
        CustomMaxPlayer = CONFIG.getCustomMaxPlayer();
    }

    public static void syncToConfig() {
        LanRuleProfile rules = CONFIG.getRuleProfile();
        rules.setAllowPvp(allowPVP);
        rules.setOnlineMode(onlineMode);
        rules.setSpawnAnimals(spawnAnimals);
        rules.setSpawnNpcs(spawnNPCs);
        rules.setAllowFlight(allowFlight);
        rules.setWhiteList(whiteList);
        rules.setBanCommands(BanCommand);
        rules.setOpCommands(OpCommand);
        rules.setSaveCommands(SaveCommand);
        rules.setHttpApi(HttpAPI);
        rules.setLanOutput(LanOutput);
        rules.setMotd(motd);
        CONFIG.setCustomPort(CustomPort);
        CONFIG.setCustomMaxPlayer(CustomMaxPlayer);
    }
}
