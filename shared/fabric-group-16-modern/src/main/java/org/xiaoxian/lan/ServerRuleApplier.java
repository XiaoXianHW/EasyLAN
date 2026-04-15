package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;

public final class ServerRuleApplier {
    private static final String[] GAME_RULE_CLASS_NAMES = {
            "net.minecraft.world.GameRules",
            "net.minecraft.world.level.GameRules"
    };

    private ServerRuleApplier() {
    }

    public static void apply(MinecraftServer server) {
        invokeBooleanSetter(server, allowPVP, "setPvpAllowed", "setPvpEnabled");
        invokeBooleanSetter(server, onlineMode, "setUsesAuthentication");
        applyMobSpawning(server);
        applyNpcSetting(server);
        invokeBooleanSetter(server, allowFlight, "setFlightAllowed", "setAllowsFlight");
        invokeStringSetter(server, motd, "setMotd");
    }

    private static void applyMobSpawning(MinecraftServer server) {
        Object gameRules = resolveGameRules(server);
        if (gameRules == null) {
            return;
        }

        Object ruleKey = resolveMobSpawningRuleKey();
        if (ruleKey == null) {
            return;
        }

        Object rule = invokeAny(gameRules, new String[] { "getRule", "get" }, new Class<?>[] { ruleKey.getClass() }, ruleKey);
        if (rule == null) {
            return;
        }

        invokeAny(rule, new String[] { "set" }, new Class<?>[] { Boolean.TYPE, MinecraftServer.class }, spawnAnimals, server);
        invokeAny(rule, new String[] { "set" }, new Class<?>[] { Boolean.TYPE }, spawnAnimals);
    }

    private static void applyNpcSetting(MinecraftServer server) {
        invokeAny(server, new String[] { "setSpawnNPCs", "setSpawnNpcs" }, new Class<?>[] { Boolean.TYPE }, spawnNPCs);
    }

    private static void invokeBooleanSetter(MinecraftServer server, boolean value, String... methodNames) {
        invokeAny(server, methodNames, new Class<?>[] { Boolean.TYPE }, value);
    }

    private static void invokeStringSetter(MinecraftServer server, String value, String... methodNames) {
        invokeAny(server, methodNames, new Class<?>[] { String.class }, value);
    }

    private static Object resolveGameRules(MinecraftServer server) {
        Object direct = invokeAny(server, new String[] { "getGameRules" }, new Class<?>[0]);
        if (direct != null) {
            return direct;
        }

        Object worldData = invokeAny(server, new String[] { "getWorldData" }, new Class<?>[0]);
        if (worldData != null) {
            Object nested = invokeAny(worldData, new String[] { "getGameRules" }, new Class<?>[0]);
            if (nested != null) {
                return nested;
            }
        }

        return null;
    }

    private static Object resolveMobSpawningRuleKey() {
        for (String className : GAME_RULE_CLASS_NAMES) {
            try {
                Class<?> clazz = Class.forName(className);
                for (String fieldName : new String[] { "RULE_DOMOBSPAWNING", "DO_MOB_SPAWNING" }) {
                    try {
                        Field field = clazz.getField(fieldName);
                        return field.get(null);
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static Object invokeAny(Object target, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        for (String methodName : methodNames) {
            try {
                Method method = findMethod(target.getClass(), methodName, parameterTypes);
                if (method == null) {
                    continue;
                }
                return method.invoke(target, args);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        try {
            Method method = type.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
