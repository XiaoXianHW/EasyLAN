package org.xiaoxian.lan;

import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerStopping {

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (event.getServer().isSingleplayer()) {
            new ShareToLan().handleStop();
        }
    }
}
