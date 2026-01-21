package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.input.ModernInputHandler;
import org.dreambot.api.utilities.Timer;

/**
 * Leaf responsible for automated advertising and interaction in-game.
 */
public class AutoChatLeaf implements Leaf {
    private final CasinoConfig config;
    private final Timer chatTimer = new Timer();
    private long nextChatTime = 0;

    public AutoChatLeaf(CasinoConfig config) {
        this.config = config;
    }

    @Override
    public boolean isValid() {
        return System.currentTimeMillis() >= nextChatTime;
    }

    @Override
    public int onLoop() {
        String message = "[Snipes] Casino is open! Trade with me for games!";
        if (message != null && !message.isEmpty()) {
            ModernInputHandler.typeText(message, true);
            // Randomize next chat interval between 5-12 seconds
            nextChatTime = System.currentTimeMillis() + (5000 + (long)(Math.random() * 7000));
        }
        return 600;
    }
}
