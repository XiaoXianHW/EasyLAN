package org.xiaoxian.lan;

import net.minecraft.command.impl.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(event.getCommandDispatcher());
        }

        if (BanCommands) {
            BanCommand.register(event.getCommandDispatcher());
            BanIpCommand.register(event.getCommandDispatcher());
            BanListCommand.register(event.getCommandDispatcher());
            PardonCommand.register(event.getCommandDispatcher());
            PardonIpCommand.register(event.getCommandDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(event.getCommandDispatcher());
            DeOpCommand.register(event.getCommandDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(event.getCommandDispatcher());
            SaveOnCommand.register(event.getCommandDispatcher());
            SaveOffCommand.register(event.getCommandDispatcher());
        }

        minecraftServer.setAllowPvp(allowPVP);
        minecraftServer.setOnlineMode(onlineMode);
        minecraftServer.setCanSpawnAnimals(spawnAnimals);
        minecraftServer.setCanSpawnNPCs(spawnNPCs);
        minecraftServer.setAllowFlight(allowFlight);
        minecraftServer.setMOTD(motd);
    }
}