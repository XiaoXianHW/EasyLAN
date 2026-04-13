package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(server.getCommands().getDispatcher());
        }

        if (BanCommands) {
            BanPlayerCommands.register(server.getCommands().getDispatcher());
            BanIpCommands.register(server.getCommands().getDispatcher());
            BanListCommands.register(server.getCommands().getDispatcher());
            PardonCommand.register(server.getCommands().getDispatcher());
            PardonIpCommand.register(server.getCommands().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(server.getCommands().getDispatcher());
            DeOpCommands.register(server.getCommands().getDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(server.getCommands().getDispatcher());
            SaveOnCommand.register(server.getCommands().getDispatcher());
            SaveOffCommand.register(server.getCommands().getDispatcher());
        }

        server.setPvpAllowed(allowPVP);
        server.setUsesAuthentication(onlineMode);
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(spawnAnimals, server);
        applyNpcSetting(server);
        server.setFlightAllowed(allowFlight);
        server.setMotd(motd);
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
