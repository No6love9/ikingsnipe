package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameContext;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.models.CasinoConfig;

/**
 * FiftyFiveGame (Dice) - Elite Titan Casino
 * 
 * Standard 55x2 dice game.
 */
public class FiftyFiveGame extends AbstractGame {

    @Override
    public String getId() { return "dice"; }

    @Override
    public String getDisplayName() { return "Dice Duel (55x2)"; }

    @Override
    public String getTrigger() { return "!55"; }

    @Override
    public double getDefaultMultiplier() { return 2.0; }

    @Override
    public GameResult play(String player, long bet, GameContext context) {
        int roll = provablyFair.generateDiceRoll();
        String reveal = provablyFair.getShortRevealString();
        
        boolean win = roll >= 55;
        double multiplier = (settings != null) ? settings.multiplier : getDefaultMultiplier();
        long payout = win ? calculatePayout(bet, multiplier) : 0;
        
        String description = String.format("%s: Rolled %d (55+ wins x%.1f) | Seed: %s", 
            win ? "WIN" : "LOSS", roll, multiplier, reveal);
        
        logGame(player, description, payout);
        return new GameResult(win, payout, description, String.valueOf(roll));
    }

    @Override
    public String getRulesDescription() {
        return "Roll 1-100. 55 or higher wins x2.0 payout.";
    }
}
