package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class FiftyFiveGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int roll = random.nextInt(100) + 1;
        boolean win = roll >= 55;
        
        long payout = win ? calculatePayout(bet) : 0;
        
        return new GameResult(win, payout,
            "Rolled " + roll + " (55+ wins)", String.valueOf(roll));
    }
}
