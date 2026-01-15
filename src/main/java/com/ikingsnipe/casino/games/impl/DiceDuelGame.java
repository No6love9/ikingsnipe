package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class DiceDuelGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int playerRoll = random.nextInt(100) + 1;
        int hostRoll = random.nextInt(100) + 1;
        boolean win = playerRoll > hostRoll;
        return new GameResult(win, win ? (long)(bet * 3.0) : 0, 
            "Rolled " + playerRoll + " vs Host " + hostRoll, playerRoll + ":" + hostRoll);
    }
}
