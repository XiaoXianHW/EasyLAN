package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.*;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(event.getServer().getCommands().getDispatcher());
        }

        if (BanCommands) {
            BanPlayerCommands.register(event.getServer().getCommands().getDispatcher());
            BanIpCommands.register(event.getServer().getCommands().getDispatcher());
            BanListCommands.register(event.getServer().getCommands().getDispatcher());
            PardonCommand.register(event.getServer().getCommands().getDispatcher());
            PardonIpCommand.register(event.getServer().getCommands().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(event.getServer().getCommands().getDispatcher());
            DeOpCommands.register(event.getServer().getCommands().getDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(event.getServer().getCommands().getDispatcher());
            SaveOnCommand.register(event.getServer().getCommands().getDispatcher());
            SaveOffCommand.register(event.getServer().getCommands().getDispatcher());
        }

        minecraftServer.setPvpAllowed(allowPVP);
        minecraftServer.setUsesAuthentication(onlineMode);
        minecraftServer.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(spawnAnimals, minecraftServer);
        minecraftServer.setFlightAllowed(allowFlight);
        minecraftServer.setMotd(motd);
    }
}