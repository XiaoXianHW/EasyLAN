package org.xiaoxian.easylan.neoforge.version;

/** Per-world limit used by both vanilla server status and player admission. */
public interface ConfiguredPlayerLimit {
    void easylan$setMaxPlayers(int maxPlayers);
}
