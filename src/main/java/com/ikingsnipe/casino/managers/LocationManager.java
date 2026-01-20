package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;

public class LocationManager {
    private final CasinoConfig config;
    public LocationManager(CasinoConfig config) { this.config = config; }
    public boolean isAtLocation() {
        return Players.getLocal().getTile().distance(config.getTargetTile()) <= 5;
    }
    public void walkToLocation() {
        if (Walking.shouldWalk(5)) Walking.walk(config.getTargetTile());
    }
}
