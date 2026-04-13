package org.xiaoxian;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.xiaoxian.easylan.core.config.EasyLanConfig;
import org.xiaoxian.easylan.core.model.LanRuleProfile;
import org.xiaoxian.easylan.core.runtime.EasyLanRuntimeState;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;
import org.xiaoxian.lan.ServerStarting;
import org.xiaoxian.lan.ServerStopping;
import org.xiaoxian.util.ConfigUtil;

@Mod(EasyLAN.MOD_ID)
public class EasyLAN {
    public static final String MOD_ID = "easylan";

    private static final EasyLanConfig CONFIG = EasyLanConfig.defaultConfig();
    private static final EasyLanRuntimeState RUNTIME_STATE = new EasyLanRuntimeState();

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
        MinecraftForge.EVENT_BUS.register(new GuiWorldSelectionEdit());
        MinecraftForge.EVENT_BUS.register(new GuiShareToLanEdit());
        MinecraftForge.EVENT_BUS.register(new ServerStarting());
        MinecraftForge.EVENT_BUS.register(new ServerStopping());

        GuiShareToLanEdit.PortText = CustomPort;
        GuiShareToLanEdit.MaxPlayerText = CustomMaxPlayer;
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
        allowFlight = rules.isAllowFlight();
        whiteList = rules.isWhiteList();
        BanCommands = rules.isBanCommands();
        OpCommands = rules.isOpCommands();
        SaveCommands = rules.isSaveCommands();
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
        rules.setAllowFlight(allowFlight);
        rules.setWhiteList(whiteList);
        rules.setBanCommands(BanCommands);
        rules.setOpCommands(OpCommands);
        rules.setSaveCommands(SaveCommands);
        rules.setHttpApi(HttpAPI);
        rules.setLanOutput(LanOutput);
        rules.setMotd(motd);
        CONFIG.setCustomPort(CustomPort);
        CONFIG.setCustomMaxPlayer(CustomMaxPlayer);
    }
}
