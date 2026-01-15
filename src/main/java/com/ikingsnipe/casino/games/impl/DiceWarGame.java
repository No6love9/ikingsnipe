package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class DiceWarGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        // In Dice War, we simulate two players rolling.
        int p1Roll = random.nextInt(100) + 1;
        int p2Roll = random.nextInt(100) + 1;
        
        // If it's a tie, re-roll
        while (p1Roll == p2Roll) {
            p1Roll = random.nextInt(100) + 1;
            p2Roll = random.nextInt(100) + 1;
        }
        
        boolean win = p1Roll > p2Roll;
        long payout = win ? calculatePayout(bet) : 0;
        
        String description = "P1: " + p1Roll + " vs P2: " + p2Roll;
        
        return new GameResult(win, payout, description, p1Roll + ":" + p2Roll);
    }
}
