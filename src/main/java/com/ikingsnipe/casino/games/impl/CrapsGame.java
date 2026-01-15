package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.Arrays;
import java.util.List;

public class CrapsGame extends AbstractGame {
    private CasinoConfig config;
    
    // State for Chasing Craps
    private boolean isB2B = false;
    private int predictedNumber = -1;

    public void setConfig(CasinoConfig config) {
        this.config = config;
    }

    public void setB2B(boolean b2b) {
        this.isB2B = b2b;
    }

    public void setPredictedNumber(int number) {
        this.predictedNumber = number;
    }

    @Override
    public GameResult play(long bet, double multiplier) {
        int d1 = roll();
        int d2 = roll();
        int total = d1 + d2;
        
        List<Integer> winningNumbers = Arrays.asList(7, 9, 12);
        if (config != null && config.games.containsKey("craps")) {
            winningNumbers = config.games.get("craps").winningNumbers;
        }
        
        boolean win = winningNumbers.contains(total);
        double finalMultiplier = multiplier; // Default x3 from config
        
        String description = "Rolled " + total;
        
        if (isB2B) {
            if (win) {
                if (predictedNumber != -1) {
                    if (total == predictedNumber) {
                        finalMultiplier = 12.0;
                        description += " (PREDICTED B2B WIN! x12)";
                    } else {
                        finalMultiplier = 9.0;
                        description += " (B2B WIN! x9 - Prediction missed)";
                    }
                } else {
                    finalMultiplier = 9.0;
                    description += " (B2B WIN! x9)";
                }
            } else {
                description += " (B2B LOSS)";
            }
        } else {
            if (win) {
                description += " (WIN! x3)";
            } else {
                description += " (LOSS)";
            }
        }

        // Reset state for next game
        isB2B = false;
        predictedNumber = -1;

        return new GameResult(win, win ? (long)(bet * finalMultiplier) : 0, description, String.valueOf(total));
    }
    
    private int roll() {
        return random.nextInt(6) + 1;
    }
}
