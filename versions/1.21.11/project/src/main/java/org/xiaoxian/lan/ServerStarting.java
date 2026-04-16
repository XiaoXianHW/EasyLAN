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
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.xiaoxian.EasyLAN.BanCommands;
import static org.xiaoxian.EasyLAN.OpCommands;
import static org.xiaoxian.EasyLAN.SaveCommands;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.whiteList;

public class ServerStarting {
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftServer minecraftServer = event.getServer();

        if (whiteList) {
            WhitelistCommand.register(minecraftServer.getCommands().getDispatcher());
        }

        if (BanCommands) {
            BanPlayerCommands.register(minecraftServer.getCommands().getDispatcher());
            BanIpCommands.register(minecraftServer.getCommands().getDispatcher());
            BanListCommands.register(minecraftServer.getCommands().getDispatcher());
            PardonCommand.register(minecraftServer.getCommands().getDispatcher());
            PardonIpCommand.register(minecraftServer.getCommands().getDispatcher());
        }

        if (OpCommands) {
            OpCommand.register(minecraftServer.getCommands().getDispatcher());
            DeOpCommands.register(minecraftServer.getCommands().getDispatcher());
        }

        if (SaveCommands) {
            SaveAllCommand.register(minecraftServer.getCommands().getDispatcher());
            SaveOnCommand.register(minecraftServer.getCommands().getDispatcher());
            SaveOffCommand.register(minecraftServer.getCommands().getDispatcher());
        }

        minecraftServer.setUsesAuthentication(onlineMode);
        minecraftServer.getWorldData().getGameRules().set(GameRules.PVP, allowPVP, minecraftServer);
        minecraftServer.getWorldData().getGameRules().set(GameRules.SPAWN_MOBS, spawnAnimals, minecraftServer);
        minecraftServer.getWorldData().getGameRules().set(GameRules.SPAWN_MONSTERS, spawnAnimals, minecraftServer);
        applyAllowFlight(minecraftServer, allowFlight);
        minecraftServer.setMotd(motd);
    }

    private void applyAllowFlight(MinecraftServer server, boolean value) {
        if (invokeBooleanSetter(server, "setAllowFlight", value)) {
            return;
        }
        if (invokeBooleanSetter(server, "setFlightAllowed", value)) {
            return;
        }
        setBooleanField(server, "allowFlight", value);
    }

    private boolean invokeBooleanSetter(MinecraftServer server, String methodName, boolean value) {
        try {
            Method method = server.getClass().getMethod(methodName, boolean.class);
            method.invoke(server, value);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private void setBooleanField(MinecraftServer server, String fieldName, boolean value) {
        Class<?> current = server.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.setBoolean(server, value);
                return;
            } catch (ReflectiveOperationException ignored) {
                current = current.getSuperclass();
            }
        }
    }
}
