package org.xiaoxian.easylan.core.model;

public class LanRuleProfile {
    private boolean allowPvp = true;
    private boolean onlineMode = true;
    private boolean spawnAnimals = true;
    private boolean spawnNpcs = true;
    private boolean allowFlight = true;
    private boolean whiteList = false;
    private boolean banCommands = false;
    private boolean opCommands = false;
    private boolean saveCommands = false;
    private boolean httpApi = true;
    private boolean lanOutput = true;
    private String motd = "This is a Default EasyLAN Motd!";

    public boolean isAllowPvp() {
        return allowPvp;
    }

    public void setAllowPvp(boolean allowPvp) {
        this.allowPvp = allowPvp;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
    }

    public boolean isSpawnAnimals() {
        return spawnAnimals;
    }

    public void setSpawnAnimals(boolean spawnAnimals) {
        this.spawnAnimals = spawnAnimals;
    }

    public boolean isSpawnNpcs() {
        return spawnNpcs;
    }

    public void setSpawnNpcs(boolean spawnNpcs) {
        this.spawnNpcs = spawnNpcs;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    public boolean isBanCommands() {
        return banCommands;
    }

    public void setBanCommands(boolean banCommands) {
        this.banCommands = banCommands;
    }

    public boolean isOpCommands() {
        return opCommands;
    }

    public void setOpCommands(boolean opCommands) {
        this.opCommands = opCommands;
    }

    public boolean isSaveCommands() {
        return saveCommands;
    }

    public void setSaveCommands(boolean saveCommands) {
        this.saveCommands = saveCommands;
    }

    public boolean isHttpApi() {
        return httpApi;
    }

    public void setHttpApi(boolean httpApi) {
        this.httpApi = httpApi;
    }

    public boolean isLanOutput() {
        return lanOutput;
    }

    public void setLanOutput(boolean lanOutput) {
        this.lanOutput = lanOutput;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }
}
