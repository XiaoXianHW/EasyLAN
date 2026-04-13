package org.xiaoxian.lan;

import net.minecraft.command.impl.BanCommand;
import net.minecraft.command.impl.BanIpCommand;
import net.minecraft.command.impl.BanListCommand;
import net.minecraft.command.impl.DeOpCommand;
import net.minecraft.command.impl.OpCommand;
import net.minecraft.command.impl.PardonCommand;
import net.minecraft.command.impl.PardonIpCommand;
import net.minecraft.command.impl.SaveAllCommand;
import net.minecraft.command.impl.SaveOffCommand;
import net.minecraft.command.impl.SaveOnCommand;
import net.minecraft.command.impl.WhitelistCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.lang.reflect.Method;

import static org.xiaoxian.EasyLAN.BanCommands;
import static org.xiaoxian.EasyLAN.OpCommands;
import static org.xiaoxian.EasyLAN.SaveCommands;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;
import static org.xiaoxian.EasyLAN.whiteList;

public class ServerStarting {
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(server.getCommandManager().getDispatcher());
        }

        if (BanCommands) {
            BanCommand.register(server.getCommandManager().getDispatcher());
            BanIpCommand.register(server.getCommandManager().getDispatcher());
            BanListCommand.register(server.getCommandManager().getDispatcher());
            PardonCommand.register(server.getCommandManager().getDispatcher());
            PardonIpCommand.register(server.getCommandManager().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(server.getCommandManager().getDispatcher());
            DeOpCommand.register(server.getCommandManager().getDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(server.getCommandManager().getDispatcher());
            SaveOnCommand.register(server.getCommandManager().getDispatcher());
            SaveOffCommand.register(server.getCommandManager().getDispatcher());
        }

        server.setAllowPvp(allowPVP);
        server.setOnlineMode(onlineMode);
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(spawnAnimals, server);
        applyNpcSetting(server);
        server.setAllowFlight(allowFlight);
        server.setMOTD(motd);
    }

    private void applyNpcSetting(MinecraftServer server) {
        invokeNpcSetter(server, "setSpawnNPCs");
        invokeNpcSetter(server, "setSpawnNpcs");
    }

    private void invokeNpcSetter(MinecraftServer server, String methodName) {
        try {
            Method method = server.getClass().getMethod(methodName, Boolean.TYPE);
            method.invoke(server, spawnNPCs);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
