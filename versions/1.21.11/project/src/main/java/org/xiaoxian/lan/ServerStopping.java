package org.xiaoxian.lan;

import net.minecraftforge.event.server.ServerStoppingEvent;

public class ServerStopping {
    public void onServerStopping(ServerStoppingEvent event) {
        if (event.getServer().isSingleplayer()) {
            new ShareToLan().handleStop();
        }
    }
}
