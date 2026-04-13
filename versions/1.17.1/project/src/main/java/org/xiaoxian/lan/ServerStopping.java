package org.xiaoxian.lan;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

public class ServerStopping {
    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (event.getServer().isSingleplayer()) {
            new ShareToLan().handleStop();
        }
    }
}
