package com.ikingsnipe.casino.games;

import java.util.List;
import java.util.Map;

public class CrapsGame extends AbstractGame {
    @Override
    public GameResult play(int betAmount, Map<String, Object> config) {
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        int total = die1 + die2;
        
        List<Integer> winningNumbers = (List<Integer>) config.get("winningNumbers");
        boolean win = winningNumbers.contains(total);
        int multiplier = (int) config.get("payoutMultiplier");
        int payout = win ? betAmount * multiplier : 0;
        
        return new GameResult(win, total, payout);
    }

    @Override
    public String getRules() {
        return "Roll 2 dice. Win on 7, 9, or 12. Payout 3x.";
    }

    @Override
    public boolean validateBet(int amount) {
        return amount >= 1000;
    }

    @Override
    public int calculatePayout(boolean win, int betAmount) {
        return win ? betAmount * 3 : 0;
    }
}
