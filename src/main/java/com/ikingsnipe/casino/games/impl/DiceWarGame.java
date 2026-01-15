package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class DiceWarGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        // In Dice War, we simulate two players rolling.
        // The bot takes a commission (multiplier is usually 1.95x instead of 2.0x)
        int p1Roll = random.nextInt(100) + 1;
        int p2Roll = random.nextInt(100) + 1;
        
        // If it's a tie, re-roll
        while (p1Roll == p2Roll) {
            p1Roll = random.nextInt(100) + 1;
            p2Roll = random.nextInt(100) + 1;
        }
        
        boolean win = p1Roll > p2Roll;
        String description = "P1: " + p1Roll + " vs P2: " + p2Roll;
        
        return new GameResult(win, win ? (long)(bet * multiplier) : 0, description, p1Roll + ":" + p2Roll);
    }
}
