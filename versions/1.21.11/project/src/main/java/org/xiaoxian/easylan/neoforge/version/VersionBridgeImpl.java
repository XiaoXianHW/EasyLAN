package org.xiaoxian.easylan.neoforge.version;

import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class VersionBridgeImpl extends ReflectionVersionBridgeSupport {
    @Override
    public boolean setMaxPlayers(Object server, int maxPlayers) {
        if (!(server instanceof IntegratedServer integratedServer)) {
            return super.setMaxPlayers(server, maxPlayers);
        }

        PlayerList playerList = integratedServer.getPlayerList();
        if (playerList instanceof EasyLanIntegratedPlayerList easyLanPlayerList) {
            easyLanPlayerList.setConfiguredMaxPlayers(maxPlayers);
            return true;
        }

        super.setMaxPlayers(server, maxPlayers);
        if (super.resolveMaxPlayers(server) == maxPlayers) {
            return true;
        }

        PlayerDataStorage playerDataStorage = findFieldValue(playerList, PlayerDataStorage.class);
        if (playerDataStorage == null) {
            playerDataStorage = findFieldValue(integratedServer, PlayerDataStorage.class);
        }
        if (playerDataStorage == null) {
            return false;
        }

        EasyLanIntegratedPlayerList replacement = new EasyLanIntegratedPlayerList(
                integratedServer,
                integratedServer.registries(),
                playerDataStorage,
                maxPlayers
        );
        copyPlayerListState(playerList, replacement);
        integratedServer.setPlayerList(replacement);
        return true;
    }

    @Override
    public int resolveMaxPlayers(Object server) {
        if (server instanceof IntegratedServer integratedServer) {
            PlayerList playerList = integratedServer.getPlayerList();
            if (playerList instanceof EasyLanIntegratedPlayerList easyLanPlayerList) {
                return easyLanPlayerList.getConfiguredMaxPlayers();
            }
        }
        return super.resolveMaxPlayers(server);
    }

    @Override
    protected String[] maxPlayerFieldNames() {
        return new String[] { "maxPlayers", "g", "f_11193_" };
    }

    private void copyPlayerListState(PlayerList source, PlayerList target) {
        Class<?> current = source.getClass();
        while (current != null && PlayerList.class.isAssignableFrom(current)) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    field.set(target, field.get(source));
                } catch (IllegalAccessException ignored) {
                }
            }
            current = current.getSuperclass();
        }
    }

    private <T> T findFieldValue(Object target, Class<T> fieldType) {
        if (target == null) {
            return null;
        }

        Class<?> current = target.getClass();
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !fieldType.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(target);
                    if (fieldType.isInstance(value)) {
                        return fieldType.cast(value);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static final class EasyLanIntegratedPlayerList extends IntegratedPlayerList {
        private int configuredMaxPlayers;

        private EasyLanIntegratedPlayerList(
                IntegratedServer server,
                LayeredRegistryAccess<RegistryLayer> registries,
                PlayerDataStorage playerDataStorage,
                int configuredMaxPlayers
        ) {
            super(server, registries, playerDataStorage);
            this.configuredMaxPlayers = configuredMaxPlayers;
        }

        @Override
        public int getMaxPlayers() {
            return configuredMaxPlayers > 0 ? configuredMaxPlayers : super.getMaxPlayers();
        }

        private int getConfiguredMaxPlayers() {
            return configuredMaxPlayers;
        }

        private void setConfiguredMaxPlayers(int configuredMaxPlayers) {
            this.configuredMaxPlayers = configuredMaxPlayers;
        }
    }
}
