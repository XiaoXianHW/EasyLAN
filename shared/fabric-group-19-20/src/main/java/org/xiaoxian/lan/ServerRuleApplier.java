package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

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
        // Vanilla has no animal only switch, doMobSpawning covers animals and NPCs at once.
        applyMobSpawningRule(minecraftServer, spawnAnimals || spawnNPCs);
        minecraftServer.setFlightAllowed(allowFlight);
        minecraftServer.setMotd(motd);
    }

    private static void applyMobSpawningRule(MinecraftServer minecraftServer, boolean enabled) {
        GameRules gameRules = minecraftServer.getGameRules();
        if (gameRules != null) {
            gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(enabled, minecraftServer);
        }
    }
}
