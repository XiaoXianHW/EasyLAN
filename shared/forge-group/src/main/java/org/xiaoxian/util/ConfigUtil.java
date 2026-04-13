package org.xiaoxian.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.xiaoxian.EasyLAN.BanCommand;
import static org.xiaoxian.EasyLAN.CustomMaxPlayer;
import static org.xiaoxian.EasyLAN.CustomPort;
import static org.xiaoxian.EasyLAN.HttpAPI;
import static org.xiaoxian.EasyLAN.LanOutput;
import static org.xiaoxian.EasyLAN.OpCommand;
import static org.xiaoxian.EasyLAN.SaveCommand;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;
import static org.xiaoxian.EasyLAN.whiteList;

public class ConfigUtil {
    private static final String CONFIG_FILE = "config/easylan.cfg";
    private static final Properties properties = new Properties();

    public static void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = Files.newInputStream(file.toPath())) {
                properties.load(input);
                allowPVP = Boolean.parseBoolean(get("pvp"));
                onlineMode = Boolean.parseBoolean(get("online-mode"));
                spawnAnimals = Boolean.parseBoolean(get("spawn-Animals"));
                spawnNPCs = Boolean.parseBoolean(get("spawn-NPCs"));
                allowFlight = Boolean.parseBoolean(get("allow-Flight"));
                whiteList = Boolean.parseBoolean(get("whiteList"));
                BanCommand = Boolean.parseBoolean(get("BanCommand"));
                OpCommand = Boolean.parseBoolean(get("OpCommand"));
                SaveCommand = Boolean.parseBoolean(get("SaveCommand"));
                HttpAPI = Boolean.parseBoolean(get("Http-Api"));
                LanOutput = Boolean.parseBoolean(get("Lan-output"));
                CustomPort = get("Port");
                CustomMaxPlayer = get("MaxPlayer");
                motd = get("Motd");
            } catch (IOException ex) {
                System.out.println("[EasyLAN] Error loading config file: " + ex.getMessage());
            }
        } else {
            setDefaultProperties();
            save();
        }
    }

    public static void save() {
        try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            properties.store(output, "EasyLAN configuration");
        } catch (IOException ex) {
            System.out.println("[EasyLAN] Error saving config file: " + ex.getMessage());
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
        properties.setProperty("BanCommand", "false");
        properties.setProperty("OpCommand", "false");
        properties.setProperty("SaveCommand", "false");
        properties.setProperty("Motd", "This is a Default EasyLAN Motd!");
        properties.setProperty("Port", "25565");
        properties.setProperty("MaxPlayer", "20");
    }
}
