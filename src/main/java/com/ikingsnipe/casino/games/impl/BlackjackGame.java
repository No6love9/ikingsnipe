package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class BlackjackGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int playerHand = drawHand();
        int host = drawHand();
        
        // Basic dealer logic: hit until 17
        while (host < 17) { 
            host += random.nextInt(10) + 1; 
        }
        
        boolean win;
        if (playerHand > 21) win = false;
        else if (host > 21) win = true;
        else win = playerHand > host;

        long payout = win ? calculatePayout(bet) : 0;

        return new GameResult(win, payout,
            String.format("Got %d vs Host %d", playerHand, host), 
            playerHand + ":" + host);
    }

    private int drawHand() {
        int total = (random.nextInt(10) + 1) + (random.nextInt(10) + 1);
        // Basic hit logic for player simulation
        if (total < 14) total += random.nextInt(10) + 1; 
        return total;
    }
}
