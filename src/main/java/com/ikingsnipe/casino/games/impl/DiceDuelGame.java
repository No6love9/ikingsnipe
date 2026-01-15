package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class DiceDuelGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int playerRoll = random.nextInt(100) + 1;
        int hostRoll = random.nextInt(100) + 1;
        
        // Tie goes to host in standard dice duel
        boolean win = playerRoll > hostRoll;
        
        long payout = win ? calculatePayout(bet) : 0;
        
        return new GameResult(win, payout, 
            String.format("Rolled %d vs Host %d", playerRoll, hostRoll), 
            playerRoll + ":" + hostRoll);
    }
}
