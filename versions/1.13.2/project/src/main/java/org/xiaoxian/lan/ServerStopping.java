package org.xiaoxian.lan;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class ServerStopping {

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (event.getServer().isSinglePlayer()) {
            new ShareToLan().handleStop();
        }
    }
}
