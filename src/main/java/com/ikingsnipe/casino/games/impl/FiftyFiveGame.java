package com.ikingsnipe.casino.games.impl;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class FiftyFiveGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int roll = random.nextInt(100) + 1;
        
        // 55x2: Roll 55 or higher to win x2
        boolean win = roll >= 55;
        
        double multiplier = 2.0;
        if (settings != null) {
            multiplier = settings.multiplier;
        }
        
        long payout = win ? (long)(bet * multiplier) : 0;
        
        String resultMsg = win ? "WIN" : "LOSS";
        
        return new GameResult(win, payout,
            String.format("%s: Rolled %d (55+ wins x%.1f)", resultMsg, roll, multiplier), 
            String.valueOf(roll));
    }
}
