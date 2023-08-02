package org.xiaoxian.lan;

import net.minecraft.command.impl.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(event.getServer().getCommandManager().getDispatcher());
        }

        if (BanCommands) {
            BanCommand.register(event.getServer().getCommandManager().getDispatcher());
            BanIpCommand.register(event.getServer().getCommandManager().getDispatcher());
            BanListCommand.register(event.getServer().getCommandManager().getDispatcher());
            PardonCommand.register(event.getServer().getCommandManager().getDispatcher());
            PardonIpCommand.register(event.getServer().getCommandManager().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(event.getServer().getCommandManager().getDispatcher());
            DeOpCommand.register(event.getServer().getCommandManager().getDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(event.getServer().getCommandManager().getDispatcher());
            SaveOnCommand.register(event.getServer().getCommandManager().getDispatcher());
            SaveOffCommand.register(event.getServer().getCommandManager().getDispatcher());
        }

        minecraftServer.setAllowPvp(allowPVP);
        minecraftServer.setOnlineMode(onlineMode);
        minecraftServer.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(spawnAnimals, minecraftServer);
        minecraftServer.setAllowFlight(allowFlight);
        minecraftServer.setMOTD(motd);
    }
}