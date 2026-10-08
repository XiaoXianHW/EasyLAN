package org.xiaoxian.easylan.forge.version;

import org.xiaoxian.easylan.core.validation.ValidationRules;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.util.Locale;

/** Publish exactly one vanilla endpoint; apply settings only after success. */
public final class LanPublication {
    private LanPublication() { }

    public interface PortSupplier {
        int get() throws IOException;
    }

    public interface Publisher<T> {
        boolean publish(T gameMode, boolean commands, int port);
    }

    public static <T> int publish(String requestedPort, T gameMode, boolean commands,
                                 PortSupplier defaultPort, Publisher<T> publisher) throws IOException {
        int port = requestedPort.isEmpty() ? defaultPort.get() : Integer.parseInt(requestedPort);
        if (!ValidationRules.isValidPort(port)) {
            throw new IllegalArgumentException("Invalid LAN port: " + port);
        }
        return publisher.publish(gameMode, commands, port) ? port : -1;
    }

    public static int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /** These vanilla screens have exactly one mode field and one boolean setting.
     * Match types on the exact vanilla class, not names that reobfuscation changes.
     * Missing or ambiguous layouts fail before any server publication.
     */
    public static <T extends Enum<T>> T selectedGameMode(Object screen, Class<?> screenClass,
                                                         Class<T> gameModeClass)
            throws ReflectiveOperationException {
        Field selected = null;
        for (Field field : screenClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    && (field.getType() == gameModeClass || field.getType() == String.class)) {
                if (selected != null) {
                    throw new NoSuchFieldException("Ambiguous vanilla LAN game-mode fields");
                }
                selected = field;
            }
        }
        if (selected == null) {
            throw new NoSuchFieldException("No vanilla LAN game-mode field");
        }
        selected.setAccessible(true);
        Object value = selected.get(screen);
        if (value == null) {
            throw new NoSuchFieldException("No vanilla LAN game-mode selection");
        }
        if (value instanceof String) {
            return Enum.valueOf(gameModeClass, ((String) value).toUpperCase(Locale.ROOT));
        }
        return gameModeClass.cast(value);
    }

    public static boolean selectedCommands(Object screen, Class<?> screenClass)
            throws ReflectiveOperationException {
        Field selected = null;
        for (Field field : screenClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Boolean.TYPE) {
                if (selected != null) {
                    throw new NoSuchFieldException("Ambiguous vanilla LAN command fields");
                }
                selected = field;
            }
        }
        if (selected == null) {
            throw new NoSuchFieldException("No vanilla LAN command field");
        }
        selected.setAccessible(true);
        return selected.getBoolean(screen);
    }
}
