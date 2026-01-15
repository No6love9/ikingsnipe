package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class FiftyFiveGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        int roll = random.nextInt(100) + 1;
        boolean win = roll >= 55;
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Rolled " + roll + " (55+ wins)", String.valueOf(roll));
    }
}
