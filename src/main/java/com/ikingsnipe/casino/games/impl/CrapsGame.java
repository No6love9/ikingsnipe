package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class CrapsGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int total = d1 + d2;
        
        // Winning numbers: 7, 9, 12. All others lose.
        boolean win = (total == 7 || total == 9 || total == 12);
        
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Rolled " + total + " (7/9/12 wins)", String.valueOf(total));
    }
}
