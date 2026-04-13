package org.xiaoxian.lan;

import org.xiaoxian.EasyLAN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiLanStatus {
    public static List<String> playerIDs = new ArrayList<>();

    public synchronized int start() throws IOException {
        return EasyLAN.getRuntimeState().startHttpApi();
    }

    public synchronized void stop() {
        EasyLAN.getRuntimeState().stopHttpApi();
    }

    public void set(String key, String value) {
        EasyLAN.getRuntimeState().getStatusSnapshot().putStatus(key, value);
        EasyLAN.getRuntimeState().getStatusSnapshot().replacePlayers(playerIDs);
    }
}
