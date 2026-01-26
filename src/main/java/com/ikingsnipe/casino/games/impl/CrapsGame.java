package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameContext;
import com.ikingsnipe.casino.games.GameResult;
import java.util.Arrays;
import java.util.List;

/**
 * CrapsGame - Elite Titan Casino
 * 
 * Custom B2B (Back-to-Back) version.
 * Rules:
 * - Natural 7 or 11 on come-out = Win (x3)
 * - 2, 3, 12 (craps) = Loss
 * - Point established (4, 5, 6, 8, 9, 10) -> roll again until point hit (Win x3) or 7 (Loss)
 * - B2B (Back-to-Back) Win = x9 payout
 */
public class CrapsGame extends AbstractGame {
    private static final List<Integer> NATURAL_WINNERS = Arrays.asList(7, 11);
    private static final List<Integer> CRAPS_LOSERS = Arrays.asList(2, 3, 12);
    private static final List<Integer> POINT_NUMBERS = Arrays.asList(4, 5, 6, 8, 9, 10);

    private boolean lastWasWin = false;
    private String lastPlayer = "";
    private Integer currentPoint = null;
    private String pointPlayer = null;

    @Override
    public String getId() { return "craps"; }

    @Override
    public String getDisplayName() { return "Chasing Craps (B2B)"; }

    @Override
    public String getTrigger() { return "!c"; }

    @Override
    public double getDefaultMultiplier() { return 3.0; }

    @Override
    public GameResult play(String player, long bet, GameContext context) {
        int[] roll = provablyFair.generateRoll();
        int d1 = roll[0], d2 = roll[1], total = roll[2];
        String reveal = provablyFair.getShortRevealString();

        boolean isWin = false;
        double multiplier = 0;
        String resultType = "LOSS";

        // Point Phase
        if (currentPoint != null && player.equals(pointPlayer)) {
            if (total == currentPoint) {
                isWin = true;
                multiplier = 3.0;
                resultType = "POINT HIT (x3)";
                currentPoint = null;
                pointPlayer = null;
            } else if (total == 7) {
                isWin = false;
                resultType = "SEVEN OUT";
                currentPoint = null;
                pointPlayer = null;
            } else {
                resultType = "ROLL AGAIN (Point: " + currentPoint + ")";
                String desc = String.format("Rolled %d+%d=%d [%s] | Seed: %s", d1, d2, total, resultType, reveal);
                return new GameResult(false, 0, desc, String.valueOf(total));
            }
        } 
        // Come-out Roll
        else {
            if (NATURAL_WINNERS.contains(total)) {
                isWin = true;
                if (lastWasWin && lastPlayer.equals(player)) {
                    multiplier = 9.0;
                    resultType = "NATURAL B2B (x9)";
                } else {
                    multiplier = 3.0;
                    resultType = "NATURAL WIN (x3)";
                }
            } else if (CRAPS_LOSERS.contains(total)) {
                isWin = false;
                resultType = "CRAPS";
            } else if (POINT_NUMBERS.contains(total)) {
                currentPoint = total;
                pointPlayer = player;
                resultType = "POINT SET: " + total;
                String desc = String.format("Rolled %d+%d=%d [%s] | Seed: %s", d1, d2, total, resultType, reveal);
                return new GameResult(false, 0, desc, String.valueOf(total));
            }
        }

        lastWasWin = isWin;
        lastPlayer = player;
        long payout = isWin ? calculatePayout(bet, multiplier) : 0;
        String description = String.format("Rolled %d+%d=%d [%s] | Seed: %s", d1, d2, total, resultType, reveal);

        logGame(player, description, payout);
        return new GameResult(isWin, payout, description, String.valueOf(total));
    }

    @Override
    public String getRulesDescription() {
        return "Natural 7/11 wins x3. Point hit wins x3. B2B natural wins x9!";
    }
}
