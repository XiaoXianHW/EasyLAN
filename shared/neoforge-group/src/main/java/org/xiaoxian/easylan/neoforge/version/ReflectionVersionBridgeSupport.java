package org.xiaoxian.easylan.neoforge.version;

import org.xiaoxian.EasyLAN;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;

public abstract class ReflectionVersionBridgeSupport implements VersionBridge {
    /** Mojang name, then the SRG name used by NeoForge 1.20.1. */
    protected static final String[] LAN_ENDPOINT_METHODS = { "startTcpServerListener", "m_9711_", "addEndpoint" };
    protected static final String[] PLAYER_LIST_METHODS = { "getPlayerList", "m_6846_" };
    protected static final String[] MAX_PLAYERS_METHODS = { "getMaxPlayers", "m_11310_" };
    protected static final String[] PORT_METHODS = { "getPort", "m_7010_", "getServerPort" };

    protected abstract String[] maxPlayerFieldNames();

    @Override
    public void openLanEndpoint(Object connection, int port) throws IOException {
        IOException lastError = null;
        for (String methodName : LAN_ENDPOINT_METHODS) {
            try {
                Method method = findMethod(connection.getClass(), methodName, InetAddress.class, Integer.TYPE);
                if (method == null) {
                    continue;
                }
                method.invoke(connection, InetAddress.getByName("0.0.0.0"), port);
                EasyLAN.getRuntimeState().setLanPort(String.valueOf(port));
                return;
            } catch (IOException ex) {
                lastError = ex;
            } catch (ReflectiveOperationException ex) {
                lastError = new IOException("Unable to open LAN endpoint by reflection.", ex);
            }
        }

        Method fallback = findMethodBySignature(connection.getClass(), Void.TYPE, InetAddress.class, Integer.TYPE);
        if (fallback != null) {
            try {
                fallback.invoke(connection, InetAddress.getByName("0.0.0.0"), port);
                EasyLAN.getRuntimeState().setLanPort(String.valueOf(port));
                return;
            } catch (IOException ex) {
                lastError = ex;
            } catch (ReflectiveOperationException ex) {
                lastError = new IOException("Unable to open LAN endpoint by reflection.", ex);
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("No supported LAN endpoint method was found on " + connection.getClass().getName() + ".");
    }

    @Override
    public boolean setMaxPlayers(Object server, int maxPlayers) {
        Object playerList = invokeNoArgs(server, PLAYER_LIST_METHODS);
        if (playerList == null) {
            System.out.println("[EasyLAN] Unable to resolve the player list of " + server.getClass().getName()
                    + ", the custom player limit was not applied.");
            return false;
        }

        if (invokeIntSetter(playerList, maxPlayers, "setMaxPlayers")) {
            return true;
        }

        if (invokeIntSetter(server, maxPlayers, "setMaxPlayers")) {
            return true;
        }

        for (String fieldName : maxPlayerFieldNames()) {
            try {
                Field field = findField(playerList.getClass(), fieldName);
                if (field == null) {
                    continue;
                }
                field.set(playerList, maxPlayers);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }

        System.out.println("[EasyLAN] No writable max player field was found on " + playerList.getClass().getName()
                + ", the custom player limit was not applied.");
        return false;
    }

    @Override
    public int resolveMaxPlayers(Object server) {
        Object playerList = invokeNoArgs(server, PLAYER_LIST_METHODS);
        Integer playerListValue = invokeIntGetter(playerList, MAX_PLAYERS_METHODS);
        if (playerListValue != null && playerListValue > 0) {
            return playerListValue;
        }

        Integer serverValue = invokeIntGetter(server, MAX_PLAYERS_METHODS);
        if (serverValue != null && serverValue > 0) {
            return serverValue;
        }

        return 0;
    }

    @Override
    public String resolveLanPort(Object server) {
        // Never let a requested/cached port or a previous world's log override
        // the current integrated server's published endpoint.
        String publishedPort = invokePortGetter(server, PORT_METHODS);
        EasyLAN.getRuntimeState().setLanPort(publishedPort);
        return publishedPort;
    }

    private String invokePortGetter(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = findMethod(target.getClass(), methodName);
                if (method == null) {
                    continue;
                }
                Object value = method.invoke(target);
                if (value instanceof Number) {
                    int port = ((Number) value).intValue();
                    if (port > 0) {
                        return String.valueOf(port);
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    protected final Object invokeNoArgs(Object target, String... methodNames) {
        if (target == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = findMethod(target.getClass(), methodName);
                if (method == null) {
                    continue;
                }
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    protected final Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
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

    protected final Method findMethodBySignature(Class<?> type, Class<?> returnType, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getReturnType() != returnType
                        || !Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    protected final Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private boolean invokeIntSetter(Object target, int value, String... methodNames) {
        if (target == null) {
            return false;
        }

        for (String methodName : methodNames) {
            try {
                Method method = findMethod(target.getClass(), methodName, Integer.TYPE);
                if (method == null) {
                    continue;
                }
                method.invoke(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }

    private Integer invokeIntGetter(Object target, String... methodNames) {
        Object value = invokeNoArgs(target, methodNames);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

}
