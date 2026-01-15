package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class HotColdGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        boolean isHot = random.nextBoolean();
        boolean win = random.nextBoolean(); // 50/50
        String outcome = isHot ? "Hot" : "Cold";
        
        long payout = win ? calculatePayout(bet) : 0;
        
        return new GameResult(win, payout,
            (win ? "Correct! " : "Incorrect. ") + "Outcome was " + outcome, outcome);
    }
}
