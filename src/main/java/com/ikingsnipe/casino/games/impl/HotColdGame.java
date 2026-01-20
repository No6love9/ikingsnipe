package com.ikingsnipe.casino.games.impl;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class HotColdGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int roll = random.nextInt(100) + 1;
        
        // Hot/Cold: 1-50 is Cold, 51-100 is Hot.
        // We simulate a 50/50 win chance for the player.
        boolean win = roll > 50;
        
        double multiplier = 2.0;
        if (settings != null) {
            multiplier = settings.multiplier;
        }
        
        long payout = win ? (long)(bet * multiplier) : 0;
        
        String type = roll > 50 ? "HOT" : "COLD";
        String resultMsg = win ? "WIN" : "LOSS";
        
        return new GameResult(win, payout,
            String.format("%s: Rolled %d (%s)", resultMsg, roll, type), 
            String.valueOf(roll));
    }
}
