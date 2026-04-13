package org.xiaoxian.easylan.core.config;

import org.xiaoxian.easylan.core.model.LanRuleProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class EasyLanConfig {
    private static final String KEY_HTTP_API = "Http-Api";
    private static final String KEY_LAN_OUTPUT = "Lan-output";
    private static final String KEY_PVP = "pvp";
    private static final String KEY_ONLINE_MODE = "online-mode";
    private static final String KEY_SPAWN_ANIMALS = "spawn-Animals";
    private static final String KEY_SPAWN_NPCS = "spawn-NPCs";
    private static final String KEY_ALLOW_FLIGHT = "allow-Flight";
    private static final String KEY_WHITE_LIST = "whiteList";
    private static final String KEY_BAN_COMMANDS = "BanCommands";
    private static final String KEY_OP_COMMANDS = "OpCommands";
    private static final String KEY_SAVE_COMMANDS = "SaveCommands";
    private static final String KEY_MOTD = "Motd";
    private static final String KEY_PORT = "Port";
    private static final String KEY_MAX_PLAYER = "MaxPlayer";

    private final Properties properties = new Properties();
    private final Path configPath;
    private final LanRuleProfile ruleProfile = new LanRuleProfile();

    private String customPort = "25565";
    private String customMaxPlayer = "20";

    public EasyLanConfig(Path configPath) {
        this.configPath = configPath;
        setDefaultProperties();
        applyProperties();
    }

    public static EasyLanConfig defaultConfig() {
        return new EasyLanConfig(Paths.get("config", "easylan.cfg"));
    }

    public synchronized void load() {
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.clear();
            properties.load(input);
        } catch (IOException ignored) {
        }

        setDefaultProperties();
        applyProperties();
    }

    public synchronized void save() {
        syncPropertiesFromState();
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "EasyLAN configuration");
            }
        } catch (IOException ignored) {
        }
    }

    public synchronized String getRawValue(String key) {
        return properties.getProperty(key);
    }

    public synchronized void setRawValue(String key, String value) {
        properties.setProperty(key, value);
        applyProperties();
    }

    public synchronized LanRuleProfile getRuleProfile() {
        return ruleProfile;
    }

    public synchronized String getCustomPort() {
        return customPort;
    }

    public synchronized void setCustomPort(String customPort) {
        this.customPort = customPort;
    }

    public synchronized String getCustomMaxPlayer() {
        return customMaxPlayer;
    }

    public synchronized void setCustomMaxPlayer(String customMaxPlayer) {
        this.customMaxPlayer = customMaxPlayer;
    }

    private void applyProperties() {
        ruleProfile.setAllowPvp(Boolean.parseBoolean(propertyOrDefault(KEY_PVP, "true")));
        ruleProfile.setOnlineMode(Boolean.parseBoolean(propertyOrDefault(KEY_ONLINE_MODE, "true")));
        ruleProfile.setSpawnAnimals(Boolean.parseBoolean(propertyOrDefault(KEY_SPAWN_ANIMALS, "true")));
        ruleProfile.setSpawnNpcs(Boolean.parseBoolean(propertyOrDefault(KEY_SPAWN_NPCS, "true")));
        ruleProfile.setAllowFlight(Boolean.parseBoolean(propertyOrDefault(KEY_ALLOW_FLIGHT, "true")));
        ruleProfile.setWhiteList(Boolean.parseBoolean(propertyOrDefault(KEY_WHITE_LIST, "false")));
        ruleProfile.setBanCommands(Boolean.parseBoolean(propertyOrDefault(KEY_BAN_COMMANDS, "false")));
        ruleProfile.setOpCommands(Boolean.parseBoolean(propertyOrDefault(KEY_OP_COMMANDS, "false")));
        ruleProfile.setSaveCommands(Boolean.parseBoolean(propertyOrDefault(KEY_SAVE_COMMANDS, "false")));
        ruleProfile.setHttpApi(Boolean.parseBoolean(propertyOrDefault(KEY_HTTP_API, "true")));
        ruleProfile.setLanOutput(Boolean.parseBoolean(propertyOrDefault(KEY_LAN_OUTPUT, "true")));
        ruleProfile.setMotd(propertyOrDefault(KEY_MOTD, "This is a Default EasyLAN Motd!"));
        customPort = propertyOrDefault(KEY_PORT, "25565");
        customMaxPlayer = propertyOrDefault(KEY_MAX_PLAYER, "20");
    }

    private void syncPropertiesFromState() {
        properties.setProperty(KEY_PVP, String.valueOf(ruleProfile.isAllowPvp()));
        properties.setProperty(KEY_ONLINE_MODE, String.valueOf(ruleProfile.isOnlineMode()));
        properties.setProperty(KEY_SPAWN_ANIMALS, String.valueOf(ruleProfile.isSpawnAnimals()));
        properties.setProperty(KEY_SPAWN_NPCS, String.valueOf(ruleProfile.isSpawnNpcs()));
        properties.setProperty(KEY_ALLOW_FLIGHT, String.valueOf(ruleProfile.isAllowFlight()));
        properties.setProperty(KEY_WHITE_LIST, String.valueOf(ruleProfile.isWhiteList()));
        properties.setProperty(KEY_BAN_COMMANDS, String.valueOf(ruleProfile.isBanCommands()));
        properties.setProperty(KEY_OP_COMMANDS, String.valueOf(ruleProfile.isOpCommands()));
        properties.setProperty(KEY_SAVE_COMMANDS, String.valueOf(ruleProfile.isSaveCommands()));
        properties.setProperty(KEY_HTTP_API, String.valueOf(ruleProfile.isHttpApi()));
        properties.setProperty(KEY_LAN_OUTPUT, String.valueOf(ruleProfile.isLanOutput()));
        properties.setProperty(KEY_MOTD, ruleProfile.getMotd());
        properties.setProperty(KEY_PORT, customPort);
        properties.setProperty(KEY_MAX_PLAYER, customMaxPlayer);
    }

    private void setDefaultProperties() {
        properties.putIfAbsent(KEY_HTTP_API, "true");
        properties.putIfAbsent(KEY_LAN_OUTPUT, "true");
        properties.putIfAbsent(KEY_PVP, "true");
        properties.putIfAbsent(KEY_ONLINE_MODE, "true");
        properties.putIfAbsent(KEY_SPAWN_ANIMALS, "true");
        properties.putIfAbsent(KEY_SPAWN_NPCS, "true");
        properties.putIfAbsent(KEY_ALLOW_FLIGHT, "true");
        properties.putIfAbsent(KEY_WHITE_LIST, "false");
        properties.putIfAbsent(KEY_BAN_COMMANDS, "false");
        properties.putIfAbsent(KEY_OP_COMMANDS, "false");
        properties.putIfAbsent(KEY_SAVE_COMMANDS, "false");
        properties.putIfAbsent(KEY_MOTD, "This is a Default EasyLAN Motd!");
        properties.putIfAbsent(KEY_PORT, "25565");
        properties.putIfAbsent(KEY_MAX_PLAYER, "20");
    }

    private String propertyOrDefault(String key, String fallback) {
        return properties.getProperty(key, fallback);
    }
}
