package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRules;

import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;

public final class ServerRuleApplier {
    private ServerRuleApplier() {
    }

    public static void apply(MinecraftServer minecraftServer) {
        minecraftServer.setUsesAuthentication(onlineMode);

        GameRules gameRules = minecraftServer.getWorldData().getGameRules();
        gameRules.set(GameRules.PVP, allowPVP, minecraftServer);
        // Vanilla has no animal only switch, spawnMobs covers animals and NPCs at once.
        gameRules.set(GameRules.SPAWN_MOBS, spawnAnimals || spawnNPCs, minecraftServer);

        // Flight is always allowed on this version, the server has no flight switch anymore.
        minecraftServer.setMotd(motd);
    }
}
