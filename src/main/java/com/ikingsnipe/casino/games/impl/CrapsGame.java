package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * ChasingCraps Implementation for SnipesScripts
 * Rules:
 * - 7, 9, 12 = Win (x3)
 * - B2B (Back-to-Back) Win = x9
 * - B2B with Prediction = x12
 */
public class CrapsGame extends AbstractGame {
    private final List<Integer> winNumbers = Arrays.asList(7, 9, 12);
    private final Random random = new Random();
    
    private boolean lastWasWin = false;
    private String lastPlayer = "";

    @Override
    public GameResult play(String player, long bet, String seed) {
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int total = d1 + d2;
        
        boolean isWin = winNumbers.contains(total);
        double multiplier = 0;
        String resultType = "LOSS";

        if (isWin) {
            if (lastWasWin && lastPlayer.equals(player)) {
                // B2B Win logic
                multiplier = 9.0;
                resultType = "B2B WIN (x9)";
            } else {
                multiplier = 3.0;
                resultType = "WIN (x3)";
            }
            lastWasWin = true;
        } else {
            lastWasWin = false;
            resultType = "LOSS";
        }
        
        lastPlayer = player;
        long payout = (long) (bet * multiplier);
        String description = "Rolled " + d1 + " + " + d2 + " = " + total + " [" + resultType + "]";

        return new GameResult(isWin, payout, description, String.valueOf(total));
    }
}
