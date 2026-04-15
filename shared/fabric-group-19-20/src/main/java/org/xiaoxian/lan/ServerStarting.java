package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.*;

import static org.xiaoxian.EasyLAN.*;

public class ServerStarting {
    public static void onServerStarting(MinecraftServer minecraftServer) {
        var dispatcher = minecraftServer.getCommands().getDispatcher();

        if (whiteList) {
            WhitelistCommand.register(dispatcher);
        }

        if (BanCommands) {
            BanPlayerCommands.register(dispatcher);
            BanIpCommands.register(dispatcher);
            BanListCommands.register(dispatcher);
            PardonCommand.register(dispatcher);
            PardonIpCommand.register(dispatcher);
        }

        if (OpCommands) {
            OpCommand.register(dispatcher);
            DeOpCommands.register(dispatcher);
        }

        if (SaveCommands) {
            SaveAllCommand.register(dispatcher);
            SaveOnCommand.register(dispatcher);
            SaveOffCommand.register(dispatcher);
        }

        ServerRuleApplier.apply(minecraftServer);
    }
}