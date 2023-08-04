package org.xiaoxian.util;

import java.io.*;
import java.util.Properties;

import static org.xiaoxian.EasyLAN.*;

public class ConfigUtil {
    private static final String CONFIG_FILE = "config/easylan.cfg";
    private static final Properties properties = new Properties();

    public static void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                allowPVP = Boolean.parseBoolean(ConfigUtil.get("pvp"));
                onlineMode = Boolean.parseBoolean(ConfigUtil.get("online-mode"));
                spawnAnimals = Boolean.parseBoolean(ConfigUtil.get("spawn-Animals"));
                spawnNPCs = Boolean.parseBoolean(ConfigUtil.get("spawn-NPCs"));
                allowFlight = Boolean.parseBoolean(ConfigUtil.get("allow-Flight"));
                whiteList = Boolean.parseBoolean(ConfigUtil.get("whiteList"));
                BanCommands = Boolean.parseBoolean(ConfigUtil.get("BanCommands"));
                OpCommands = Boolean.parseBoolean(ConfigUtil.get("OpCommands"));
                SaveCommands = Boolean.parseBoolean(ConfigUtil.get("SaveCommands"));
                HttpAPI = Boolean.parseBoolean(ConfigUtil.get("Http-Api"));
                LanOutput = Boolean.parseBoolean(ConfigUtil.get("Lan-output"));
                motd = ConfigUtil.get("Motd");
                CustomPort = ConfigUtil.get("Port");
                CustomMaxPlayer = ConfigUtil.get("MaxPlayer");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            setDefaultProperties();
            save();
        }
    }

    public static void save() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "EasyLAN configuration");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    private static void setDefaultProperties() {
        properties.setProperty("Http-Api", "true");
        properties.setProperty("Lan-output", "true");
        properties.setProperty("pvp", "true");
        properties.setProperty("online-mode", "true");
        properties.setProperty("spawn-Animals", "true");
        properties.setProperty("spawn-NPCs", "true");
        properties.setProperty("allow-Flight", "true");
        properties.setProperty("whiteList", "false");
        properties.setProperty("BanCommands", "false");
        properties.setProperty("OpCommands", "false");
        properties.setProperty("SaveCommands", "false");
        properties.setProperty("Motd", "This is a Default EasyLAN Motd!");
        properties.setProperty("Port", "25565");
        properties.setProperty("MaxPlayer", "20");
    }
}
