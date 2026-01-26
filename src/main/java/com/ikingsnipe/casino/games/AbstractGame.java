package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.ProvablyFairCraps;
import org.dreambot.api.utilities.Logger;

/**
 * AbstractGame - Elite Titan Casino
 * 
 * Base implementation for all games, providing provably fair utilities
 * and configuration management.
 */
public abstract class AbstractGame implements Game {
    protected CasinoConfig.GameSettings settings;
    protected final ProvablyFairCraps provablyFair;
    protected boolean enabled = true;

    public AbstractGame() {
        this.provablyFair = new ProvablyFairCraps();
    }

    @Override
    public void configure(CasinoConfig.GameSettings settings) {
        this.settings = settings;
        if (settings != null) {
            this.enabled = settings.enabled;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (settings != null) {
            settings.enabled = enabled;
        }
    }

    @Override
    public boolean isProvablyFair() {
        return true; // All professional games in v9.0 are provably fair
    }

    @Override
    public String getPreRollCommitment() {
        return provablyFair.getShortCommitmentHash();
    }

    @Override
    public String getFullCommitment() {
        return provablyFair.getCommitmentHash();
    }

    @Override
    public String getRevealString() {
        return provablyFair.getRevealString();
    }

    protected long calculatePayout(long bet, double multiplier) {
        return (long) (bet * multiplier);
    }

    protected void logGame(String player, String description, long payout) {
        Logger.log(String.format("[%s] %s - %s - Payout: %d", getId(), player, description, payout));
    }
}
