package org.xiaoxian.easylan.fabric.version;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.xiaoxian.EasyLAN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReflectionVersionBridgeSupport implements VersionBridge {
    protected abstract String[] maxPlayerFieldNames();

    @Override
    public void openLanEndpoint(Object connection, int port) throws IOException {
        IOException lastError = null;
        for (String methodName : new String[] { "bind", "method_14354", "a", "startTcpServerListener", "addEndpoint" }) {
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

        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("No supported LAN endpoint method was found.");
    }

    @Override
    public boolean setMaxPlayers(Object server, int maxPlayers) {
        Object playerList = invokeNoArgs(server, "getPlayerList");
        if (playerList == null) {
            return false;
        }

        for (String fieldName : maxPlayerFieldNames()) {
            try {
                Field field = findField(playerList.getClass(), fieldName);
                if (field == null) {
                    continue;
                }
                field.setAccessible(true);
                field.set(playerList, maxPlayers);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }

    @Override
    public String resolveLanPort(Object server) {
        String runtimePort = EasyLAN.getRuntimeState().getLanPort();
        if (runtimePort != null && !runtimePort.isEmpty()) {
            return runtimePort;
        }

        String reflectedPort = invokePortGetter(server, "getPort", "getServerPort");
        if (reflectedPort != null) {
            EasyLAN.getRuntimeState().setLanPort(reflectedPort);
            return reflectedPort;
        }

        String logPort = readLanPortFromLog();
        if (logPort != null) {
            EasyLAN.getRuntimeState().setLanPort(logPort);
        }
        return logPort;
    }

    @Override
    public Screen resolveWorldSelectionParent(Screen screen) {
        return resolveParentScreen(screen, new TitleScreen(), "lastScreen", "parent", "previousScreen");
    }

    @Override
    public Screen resolveShareToLanParent(Screen screen) {
        return resolveParentScreen(screen, new PauseScreen(true), "lastScreen", "parent", "previousScreen");
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

    private Object invokeNoArgs(Object target, String... methodNames) {
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

    private Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
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

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Screen resolveParentScreen(Screen screen, Screen fallback, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Screen resolved = readScreenField(screen, findField(screen.getClass(), fieldName));
            if (resolved != null && resolved != screen) {
                return resolved;
            }
        }

        Class<?> current = screen.getClass();
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (!Screen.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                Screen resolved = readScreenField(screen, field);
                if (resolved != null && resolved != screen) {
                    return resolved;
                }
            }
            current = current.getSuperclass();
        }

        return fallback;
    }

    private Screen readScreenField(Screen screen, Field field) {
        if (field == null) {
            return null;
        }

        try {
            field.setAccessible(true);
            Object value = field.get(screen);
            return value instanceof Screen ? (Screen) value : null;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private String readLanPortFromLog() {
        Pattern[] patterns = new Pattern[] {
                Pattern.compile("Started serving on ([0-9]+)"),
                Pattern.compile("Started on ([0-9]+)")
        };

        try (BufferedReader reader = new BufferedReader(new FileReader("logs/latest.log"))) {
            String line;
            String lastPort = null;
            while ((line = reader.readLine()) != null) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        lastPort = matcher.group(1);
                    }
                }
            }
            return lastPort;
        } catch (IOException ignored) {
            return null;
        }
    }
}
