package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.Random;

/**
 * Enhanced Base Game Class for snipes♧scripts Enterprise
 * Supports multiplier-based payouts and complex game states
 */
public abstract class AbstractGame {
    protected final Random random = new Random();
    protected CasinoConfig.GameSettings settings;

    public void setSettings(CasinoConfig.GameSettings settings) {
        this.settings = settings;
    }

    /**
     * Core game execution logic
     * @param player Name of the player
     * @param bet Amount bet in GP
     * @param seed Provably fair seed
     * @return GameResult containing win status and payout
     */
    public abstract GameResult play(String player, long bet, String seed);

    /**
     * Calculate payout based on multiplier in settings
     */
    protected long calculatePayout(long bet) {
        if (settings == null) return (long)(bet * 2.0);
        return (long)(bet * settings.multiplier);
    }
}
