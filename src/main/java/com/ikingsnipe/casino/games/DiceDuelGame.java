package com.ikingsnipe.casino.games;

import java.util.Map;

public class DiceDuelGame extends AbstractGame {
    @Override
    public GameResult play(int betAmount, Map<String, Object> config) {
        int playerRoll = random.nextInt(100) + 1;
        int hostRoll = random.nextInt(100) + 1;
        
        boolean win = playerRoll > hostRoll;
        int payout = win ? betAmount * 2 : 0;
        
        return new GameResult(win, playerRoll, payout);
    }

    @Override
    public String getRules() {
        return "Roll 1-100. Higher roll wins. Payout 2x.";
    }

    @Override
    public boolean validateBet(int amount) {
        return amount >= 1000;
    }

    @Override
    public int calculatePayout(boolean win, int betAmount) {
        return win ? betAmount * 2 : 0;
    }
}
