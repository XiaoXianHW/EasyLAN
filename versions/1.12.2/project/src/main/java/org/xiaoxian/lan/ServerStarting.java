package org.xiaoxian.lan;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandServerKick;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.server.CommandBanIp;
import net.minecraft.command.server.CommandBanPlayer;
import net.minecraft.command.server.CommandDeOp;
import net.minecraft.command.server.CommandListBans;
import net.minecraft.command.server.CommandOp;
import net.minecraft.command.server.CommandPardonIp;
import net.minecraft.command.server.CommandPardonPlayer;
import net.minecraft.command.server.CommandSaveAll;
import net.minecraft.command.server.CommandSaveOff;
import net.minecraft.command.server.CommandSaveOn;
import net.minecraft.command.server.CommandWhitelist;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();
        ICommandManager commandManager = minecraftServer.getCommandManager();

        if (commandManager instanceof CommandHandler) {
            CommandHandler handler = (CommandHandler) commandManager;

            if (whiteList) {
                handler.registerCommand(new CommandWhitelist());
            }

            if (BanCommand) {
                handler.registerCommand(new CommandBanPlayer());
                handler.registerCommand(new CommandBanIp());
                handler.registerCommand(new CommandListBans());
                handler.registerCommand(new CommandPardonIp());
                handler.registerCommand(new CommandPardonPlayer());
                handler.registerCommand(new CommandServerKick());
            }

            if (OpCommand) {
                handler.registerCommand(new CommandOp());
                handler.registerCommand(new CommandDeOp());
            }

            if (SaveCommand) {
                handler.registerCommand(new CommandSaveAll());
                handler.registerCommand(new CommandSaveOff());
                handler.registerCommand(new CommandSaveOn());
            }
        }

        minecraftServer.setAllowPvp(allowPVP);
        minecraftServer.setOnlineMode(onlineMode);
        minecraftServer.setCanSpawnAnimals(spawnAnimals);
        minecraftServer.setCanSpawnNPCs(spawnNPCs);
        minecraftServer.setAllowFlight(allowFlight);
        minecraftServer.setMOTD(motd);
    }
}
