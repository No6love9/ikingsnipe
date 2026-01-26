package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameContext;
import com.ikingsnipe.casino.games.GameResult;

/**
 * HotColdGame - Elite Titan Casino
 * 
 * Player chooses Hot (51-100) or Cold (1-50).
 */
public class HotColdGame extends AbstractGame {

    @Override
    public String getId() { return "hotcold"; }

    @Override
    public String getDisplayName() { return "Hot or Cold"; }

    @Override
    public String getTrigger() { return "!hc"; }

    @Override
    public double getDefaultMultiplier() { return 2.0; }

    @Override
    public GameResult play(String player, long bet, GameContext context) {
        String choice = context != null ? context.getString("choice") : "hot";
        if (choice == null) choice = "hot";
        
        int roll = provablyFair.generateDiceRoll();
        String reveal = provablyFair.getShortRevealString();
        
        boolean isHot = roll > 50;
        boolean win = (choice.equalsIgnoreCase("hot") && isHot) || (choice.equalsIgnoreCase("cold") && !isHot);
        
        double multiplier = (settings != null) ? settings.multiplier : getDefaultMultiplier();
        long payout = win ? calculatePayout(bet, multiplier) : 0;
        
        String outcome = isHot ? "HOT" : "COLD";
        String description = String.format("%s: Rolled %d (%s) | Choice: %s | Seed: %s", 
            win ? "WIN" : "LOSS", roll, outcome, choice.toUpperCase(), reveal);
        
        logGame(player, description, payout);
        return new GameResult(win, payout, description, String.valueOf(roll));
    }

    @Override
    public String getRulesDescription() {
        return "Choose Hot (51-100) or Cold (1-50). Correct guess wins x2.0.";
    }
}
