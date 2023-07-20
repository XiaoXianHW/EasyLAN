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
            WhitelistCommand.register(event.getServer().getCommands().getDispatcher());
        }

        if (BanCommands) {
            BanCommand.register(event.getServer().getCommands().getDispatcher());
            BanIpCommand.register(event.getServer().getCommands().getDispatcher());
            BanListCommand.register(event.getServer().getCommands().getDispatcher());
            PardonCommand.register(event.getServer().getCommands().getDispatcher());
            PardonIpCommand.register(event.getServer().getCommands().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(event.getServer().getCommands().getDispatcher());
            DeOpCommand.register(event.getServer().getCommands().getDispatcher());
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