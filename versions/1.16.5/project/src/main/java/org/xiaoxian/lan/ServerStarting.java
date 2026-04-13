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
            WhitelistCommand.register(server.getCommands().getDispatcher());
        }

        if (BanCommands) {
            BanCommand.register(server.getCommands().getDispatcher());
            BanIpCommand.register(server.getCommands().getDispatcher());
            BanListCommand.register(server.getCommands().getDispatcher());
            PardonCommand.register(server.getCommands().getDispatcher());
            PardonIpCommand.register(server.getCommands().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(server.getCommands().getDispatcher());
            DeOpCommand.register(server.getCommands().getDispatcher());
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
