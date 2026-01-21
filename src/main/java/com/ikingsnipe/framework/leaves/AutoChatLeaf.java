package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.utilities.Calculations;
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
        return config.autoChatEnabled && System.currentTimeMillis() >= nextChatTime;
    }

    @Override
    public int onLoop() {
        String message = config.getRandomAdvertisingMessage();
        if (message != null && !message.isEmpty()) {
            Keyboard.type(message, true);
            // Randomize next chat interval between 5-12 seconds
            nextChatTime = System.currentTimeMillis() + Calculations.random(5000, 12000);
        }
        return 600;
    }
}
