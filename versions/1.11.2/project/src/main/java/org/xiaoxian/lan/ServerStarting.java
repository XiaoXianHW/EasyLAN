package org.xiaoxian.lan;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandServerKick;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.server.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();
        ICommandManager commandManager = minecraftServer.getCommandManager();
        if (commandManager instanceof CommandHandler) {
            if (whiteList) {
                ((CommandHandler) commandManager).registerCommand(new CommandWhitelist());
            }

            if (BanCommand) {
                ((CommandHandler) commandManager).registerCommand(new CommandBanPlayer());
                ((CommandHandler) commandManager).registerCommand(new CommandBanIp());
                ((CommandHandler) commandManager).registerCommand(new CommandListBans());
                ((CommandHandler) commandManager).registerCommand(new CommandPardonIp());
                ((CommandHandler) commandManager).registerCommand(new CommandPardonPlayer());
                ((CommandHandler) commandManager).registerCommand(new CommandServerKick());
            }

            if (OpCommand) {
                ((CommandHandler) commandManager).registerCommand(new CommandOp());
                ((CommandHandler) commandManager).registerCommand(new CommandDeOp());
            }

            if (SaveCommand) {
                ((CommandHandler) commandManager).registerCommand(new CommandSaveAll());
                ((CommandHandler) commandManager).registerCommand(new CommandSaveOff());
                ((CommandHandler) commandManager).registerCommand(new CommandSaveOn());
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