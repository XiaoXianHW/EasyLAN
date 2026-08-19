package org.xiaoxian;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.xiaoxian.easylan.core.config.EasyLanConfig;
import org.xiaoxian.easylan.core.model.LanRuleProfile;
import org.xiaoxian.easylan.core.runtime.EasyLanRuntimeState;
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
    public static boolean spawnNPCs = true;
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
        NeoForge.EVENT_BUS.register(new ServerStarting());
        NeoForge.EVENT_BUS.register(new ServerStopping());

        if (isClientDist()) {
            ClientRegistrar.register(CustomPort, CustomMaxPlayer);
        }
    }

    /**
     * FMLEnvironment exposes the running side as a field on 1.20.1 and as a getter on newer versions,
     * so it is read reflectively to keep this class usable on every supported version.
     */
    private static boolean isClientDist() {
        try {
            Class<?> environment = Class.forName("net.neoforged.fml.loading.FMLEnvironment");
            Object dist;
            try {
                dist = environment.getMethod("getDist").invoke(null);
            } catch (NoSuchMethodException ex) {
                dist = environment.getField("dist").get(null);
            }
            return dist instanceof Enum && "CLIENT".equals(((Enum<?>) dist).name());
        } catch (ReflectiveOperationException ex) {
            System.out.println("[EasyLAN] Unable to detect the running side, assuming a dedicated server.");
            return false;
        }
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
        rules.setSpawnNpcs(spawnNPCs);
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
