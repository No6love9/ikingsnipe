package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class BlackjackGame extends AbstractGame {
    @Override
    public GameResult play(long bet, double multiplier) {
        int player = drawHand();
        int host = drawHand();
        
        // Basic dealer logic: hit until 17
        while (host < 17) { host += random.nextInt(10) + 1; }
        
        boolean win;
        if (player > 21) win = false;
        else if (host > 21) win = true;
        else win = player > host;

        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Got " + player + " vs Host " + host, player + ":" + host);
    }

    private int drawHand() {
        int total = (random.nextInt(10) + 1) + (random.nextInt(10) + 1);
        if (total < 12) total += random.nextInt(10) + 1; // Basic hit
        return total;
    }
}
