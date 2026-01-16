package com.ikingsnipe.casino.models;

import java.util.*;

public class AdminConfig {
    public boolean isAdminModeEnabled = false;
    public String ownerName = "";
    public List<String> adminNames = new ArrayList<>();
    public boolean enableRemoteControl = false;
    public boolean allowOwnerWithdraw = true;
    public boolean notifyOwnerOnMule = true;
    public boolean autoAcceptOwnerTrade = true;
    public boolean logAdminActions = true;
    
    // Missing fields found during compilation
    public boolean emergencyStop = false;
    public boolean disableAllGames = false;
    public boolean enableVerboseAdminLogs = false;
    public String adminPassword = "password";
    public List<String> blacklistedPlayers = new ArrayList<>();
    
    public AdminConfig() {
    }
    
    public void blacklistPlayer(String name) {
        if (!blacklistedPlayers.contains(name)) {
            blacklistedPlayers.add(name);
        }
    }
}
