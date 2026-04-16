package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;

public final class ServerRuleApplier {
    private static final String[] GAME_RULE_CLASSES = {
            "net.minecraft.world.level.GameRules",
            "net.minecraft.world.level.gamerules.GameRules"
    };

    private ServerRuleApplier() {
    }

    public static void apply(MinecraftServer minecraftServer) {
        invokeBooleanSetter(minecraftServer, allowPVP, "setPvpAllowed", "setPvpEnabled");
        invokeBooleanSetter(minecraftServer, onlineMode, "setUsesAuthentication");
        applyMobSpawningRule(minecraftServer, spawnAnimals);
        invokeBooleanSetter(minecraftServer, allowFlight, "setFlightAllowed", "setAllowsFlight");
        invokeStringSetter(minecraftServer, motd, "setMotd");
    }

    private static void applyMobSpawningRule(MinecraftServer minecraftServer, boolean enabled) {
        try {
            Object rules = resolveGameRules(minecraftServer);
            Class<?> gameRulesClass = resolveGameRulesClass();
            if (rules == null || gameRulesClass == null) {
                return;
            }

            Field ruleField = gameRulesClass.getField("RULE_DOMOBSPAWNING");
            Object ruleKey = ruleField.get(null);
            Method getRuleMethod = rules.getClass().getMethod("getRule", ruleKey.getClass());
            Object rule = getRuleMethod.invoke(rules, ruleKey);
            if (rule == null) {
                return;
            }

            Method setMethod = resolveBooleanRuleSetter(rule.getClass(), minecraftServer.getClass());
            if (setMethod == null) {
                return;
            }

            if (setMethod.getParameterCount() == 2) {
                setMethod.invoke(rule, enabled, minecraftServer);
            } else {
                setMethod.invoke(rule, enabled);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Object resolveGameRules(MinecraftServer minecraftServer) throws ReflectiveOperationException {
        Method getWorldDataMethod = findMethod(minecraftServer.getClass(), "getWorldData");
        if (getWorldDataMethod != null) {
            Object worldData = getWorldDataMethod.invoke(minecraftServer);
            if (worldData != null) {
                Method getGameRulesMethod = findMethod(worldData.getClass(), "getGameRules");
                if (getGameRulesMethod != null) {
                    return getGameRulesMethod.invoke(worldData);
                }
            }
        }

        Method getGameRulesMethod = findMethod(minecraftServer.getClass(), "getGameRules");
        return getGameRulesMethod != null ? getGameRulesMethod.invoke(minecraftServer) : null;
    }

    private static Class<?> resolveGameRulesClass() {
        for (String className : GAME_RULE_CLASSES) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }

        return null;
    }

    private static Method resolveBooleanRuleSetter(Class<?> ruleClass, Class<?> serverClass) {
        for (Method method : ruleClass.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!"set".equals(method.getName()) || parameterTypes.length == 0 || parameterTypes[0] != boolean.class) {
                continue;
            }

            if (parameterTypes.length == 1) {
                return method;
            }

            if (parameterTypes.length == 2 && parameterTypes[1].isAssignableFrom(serverClass)) {
                return method;
            }
        }

        return null;
    }

    private static void invokeBooleanSetter(Object target, boolean value, String... methodNames) {
        for (String methodName : methodNames) {
            Method method = findMethod(target.getClass(), methodName, boolean.class);
            if (method != null) {
                try {
                    method.invoke(target, value);
                    return;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        for (String fieldName : methodNames) {
            if (trySetBooleanField(target, fieldName, value)) {
                return;
            }
        }
    }

    private static void invokeStringSetter(Object target, String value, String... methodNames) {
        for (String methodName : methodNames) {
            Method method = findMethod(target.getClass(), methodName, String.class);
            if (method != null) {
                try {
                    method.invoke(target, value);
                    return;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
    }

    private static boolean trySetBooleanField(Object target, String methodLikeName, boolean value) {
        String candidate = methodLikeName.startsWith("set") && methodLikeName.length() > 3
                ? Character.toLowerCase(methodLikeName.charAt(3)) + methodLikeName.substring(4)
                : methodLikeName;

        Field field = findField(target.getClass(), candidate, boolean.class);
        if (field == null) {
            return false;
        }

        try {
            field.setAccessible(true);
            field.setBoolean(target, value);
            return true;
        } catch (IllegalAccessException ignored) {
            return false;
        }
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

        return null;
    }

    private static Field findField(Class<?> type, String name, Class<?> fieldType) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                if (field.getType() == fieldType) {
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
            }
            current = current.getSuperclass();
        }

        return null;
    }
}
