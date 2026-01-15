package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class BlackjackGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int playerHand = drawHand();
        int host = drawHand();
        
        // Basic dealer logic: hit until 17
        while (host < 17) { host += random.nextInt(10) + 1; }
        
        boolean win;
        if (playerHand > 21) win = false;
        else if (host > 21) win = true;
        else win = playerHand > host;

        return new GameResult(win, win ? (long)(bet * 3.0) : 0,
            "Got " + playerHand + " vs Host " + host, playerHand + ":" + host);
    }

    private int drawHand() {
        int total = (random.nextInt(10) + 1) + (random.nextInt(10) + 1);
        if (total < 12) total += random.nextInt(10) + 1; // Basic hit
        return total;
    }
}
