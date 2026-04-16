package org.xiaoxian.easylan.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EasyLanStatusSnapshot {
    private final Map<String, String> status = new LinkedHashMap<>();
    private final List<String> playerIds = new ArrayList<>();

    public synchronized void putStatus(String key, String value) {
        status.put(key, value);
    }

    public synchronized Map<String, String> copyStatus() {
        return new LinkedHashMap<>(status);
    }

    public synchronized void replacePlayers(List<String> players) {
        playerIds.clear();
        playerIds.addAll(players);
    }

    public synchronized List<String> copyPlayers() {
        return new ArrayList<>(playerIds);
    }

    public synchronized void clear() {
        status.clear();
        playerIds.clear();
    }
}
