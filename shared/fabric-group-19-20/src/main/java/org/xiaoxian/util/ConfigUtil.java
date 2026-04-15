package org.xiaoxian.util;

import org.xiaoxian.EasyLAN;

public class ConfigUtil {
    public static void load() {
        EasyLAN.getConfig().load();
        EasyLAN.syncFromConfig();
    }

    public static void save() {
        EasyLAN.syncToConfig();
        EasyLAN.getConfig().save();
    }

    public static String get(String key) {
        return EasyLAN.getConfig().getRawValue(key);
    }

    public static void set(String key, String value) {
        EasyLAN.getConfig().setRawValue(key, value);
    }
}