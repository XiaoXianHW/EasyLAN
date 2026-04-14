package org.xiaoxian.lan;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import static org.xiaoxian.EasyLAN.BanCommands;
import static org.xiaoxian.EasyLAN.OpCommands;
import static org.xiaoxian.EasyLAN.SaveCommands;
import static org.xiaoxian.EasyLAN.whiteList;

public class ServerStarting {
    public static void onServerStarting(MinecraftServer server) {
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

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

        ServerRuleApplier.apply(server);
    }
}
