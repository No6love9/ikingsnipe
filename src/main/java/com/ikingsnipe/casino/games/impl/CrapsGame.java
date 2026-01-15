package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class CrapsGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int total = d1 + d2;
        boolean win = (total == 7 || total == 11);
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Rolled " + total + " (7/11 wins)", String.valueOf(total));
    }
}
