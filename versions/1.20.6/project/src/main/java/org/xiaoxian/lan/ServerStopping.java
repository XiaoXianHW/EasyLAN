package org.xiaoxian.lan;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ServerStopping {

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (event.getServer().isSingleplayer()) {
            new ShareToLan().handleStop();
        }
    }
}
