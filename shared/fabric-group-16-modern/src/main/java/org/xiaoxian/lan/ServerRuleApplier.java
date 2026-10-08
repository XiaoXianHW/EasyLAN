package org.xiaoxian.lan;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.lang.reflect.Method;

import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;

public final class ServerRuleApplier {
    private ServerRuleApplier() {
    }

    public static void apply(MinecraftServer server) {
        server.setPvpAllowed(allowPVP);
        server.setUsesAuthentication(onlineMode);
        GameRules gameRules = server.getGameRules();
        if (gameRules != null) {
            gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(spawnAnimals, server);
        }
        applyNpcSetting(server);
        server.setFlightAllowed(allowFlight);
        // Keep an explicit blank MOTD; never pass a null string to chat/status serialization.
        server.setMotd(motd == null ? "" : motd);
        // 1.16 caches this description before SERVER_STARTED. setMotd only updates
        // the field used by LAN announcements/HTTP, not the saved-server ping reply.
        server.getStatus().setDescription(new TextComponent(server.getMotd()));
    }

    private static void applyNpcSetting(MinecraftServer server) {
        for (String methodName : new String[] { "setSpawnNPCs", "setSpawnNpcs" }) {
            try {
                Method method = server.getClass().getMethod(methodName, Boolean.TYPE);
                method.invoke(server, spawnNPCs);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }
}
