package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.Arrays;
import java.util.List;

public class CrapsGame extends AbstractGame {
    private static final List<Integer> WINNING_NUMBERS = Arrays.asList(7, 9, 12);
    
    // State for Chasing Craps
    private boolean isB2B = false;
    private int predictedNumber = -1;

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
        
        boolean win = WINNING_NUMBERS.contains(total);
        double finalMultiplier = multiplier; // Default x3 from config
        
        String description = "Rolled " + total;
        
        if (isB2B) {
            if (win) {
                if (predictedNumber != -1) {
                    if (total == predictedNumber) {
                        finalMultiplier = 12.0;
                        description += " (PREDICTED B2B WIN! x12)";
                    } else {
                        // Won the roll but not the prediction? 
                        // Usually in these scripts, if you predict and miss the prediction but hit a win number, 
                        // it might still be a win or a loss depending on strictness.
                        // User said: "itll be a x12 payout since they called the winning roll prior"
                        // We'll treat it as a standard B2B win if they hit a win number but not the specific one.
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
