package org.xiaoxian.lan;

import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ServerStopping {
    public void onServerStopping(FMLServerStoppingEvent event) {
        new ShareToLan().handleStop();
    }
}
