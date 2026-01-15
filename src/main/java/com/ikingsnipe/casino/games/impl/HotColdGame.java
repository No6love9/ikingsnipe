package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class HotColdGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        boolean isHot = random.nextBoolean();
        boolean win = random.nextBoolean(); // 50/50
        String outcome = isHot ? "Hot" : "Cold";
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            (win ? "Correct! " : "Incorrect. ") + "Outcome was " + outcome, outcome);
    }
}
