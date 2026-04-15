package org.xiaoxian.lan;

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

    public static void apply(MinecraftServer minecraftServer) {
        minecraftServer.setPvpAllowed(allowPVP);
        minecraftServer.setUsesAuthentication(onlineMode);
        minecraftServer.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(spawnAnimals, minecraftServer);
        applyNpcSetting(minecraftServer);
        minecraftServer.setFlightAllowed(allowFlight);
        minecraftServer.setMotd(motd);
    }

    private static void applyNpcSetting(MinecraftServer minecraftServer) {
        invokeNpcSetter(minecraftServer, "setSpawnNPCs");
        invokeNpcSetter(minecraftServer, "setSpawnNpcs");
    }

    private static void invokeNpcSetter(MinecraftServer minecraftServer, String methodName) {
        try {
            Method method = minecraftServer.getClass().getMethod(methodName, Boolean.TYPE);
            method.invoke(minecraftServer, spawnNPCs);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
