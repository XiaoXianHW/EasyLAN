package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

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
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(spawnAnimals, server);
        applyNpcSetting(server);
        server.setFlightAllowed(allowFlight);
        server.setMotd(motd);
    }

    private static void applyNpcSetting(MinecraftServer server) {
        invokeNpcSetter(server, "setSpawnNPCs");
        invokeNpcSetter(server, "setSpawnNpcs");
    }

    private static void invokeNpcSetter(MinecraftServer server, String methodName) {
        try {
            Method method = server.getClass().getMethod(methodName, Boolean.TYPE);
            method.invoke(server, spawnNPCs);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
