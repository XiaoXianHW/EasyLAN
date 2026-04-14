package org.xiaoxian.lan;

import net.minecraft.server.MinecraftServer;

public class ServerStopping {
    public static void onServerStopping(MinecraftServer server) {
        if (server.isSinglePlayer()) {
            new ShareToLan().handleStop();
        }
    }
}
