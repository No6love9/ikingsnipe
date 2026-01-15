package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

public class HumanizationManager {
    private final CasinoConfig config;
    private long lastBreakTime;
    private boolean onBreak = false;

    public HumanizationManager(CasinoConfig config) {
        this.config = config;
        this.lastBreakTime = System.currentTimeMillis();
    }

    public boolean shouldTakeBreak() {
        if (!config.enableMicroBreaks) return false;
        return System.currentTimeMillis() - lastBreakTime > (config.breakFrequencyMinutes * 60000L);
    }

    public void takeBreak() {
        onBreak = true;
        long duration = config.breakDurationMinutes * 60000L;
        Logger.log("Taking a micro-break for " + config.breakDurationMinutes + " minutes...");
        
        long breakEnd = System.currentTimeMillis() + duration;
        while (System.currentTimeMillis() < breakEnd) {
            // Occasionally move camera or mouse slightly during break
            if (Calculations.random(1, 20) == 1) {
                Camera.rotateTo(Calculations.random(0, 2048), Calculations.random(128, 383));
            }
            Sleep.sleep(5000, 15000);
        }
        
        lastBreakTime = System.currentTimeMillis();
        onBreak = false;
        Logger.log("Break finished. Resuming hosting.");
    }

    public void applyIdleHumanization() {
        if (config.enableCameraJitter && Calculations.random(1, 100) == 1) {
            Camera.rotateTo(Camera.getYaw() + Calculations.random(-100, 100), Camera.getPitch() + Calculations.random(-20, 20));
        }
        
        if (config.enableMouseFatigue && Calculations.random(1, 150) == 1) {
            Mouse.moveOutsideScreen();
        }
    }

    public boolean isOnBreak() {
        return onBreak;
    }

    public String obfuscateText(String text) {
        // Simple obfuscation for anti-mute
        return text.replace("a", "@").replace("e", "3").replace("i", "1").replace("o", "0");
    }
}
