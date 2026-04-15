package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;

public class ServerStopping {
    public static void onServerStopping(MinecraftServer minecraftServer) {
        if (minecraftServer.isSingleplayer()) {
            new ShareToLan().handleStop();
        }
    }
}
