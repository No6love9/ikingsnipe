package com.ikingsnipe.casino.managers;

import java.util.ArrayList;
import java.util.List;

public class ProfitTracker {
    private long totalWon = 0;
    private long totalLost = 0;
    private long startTime;
    private final List<String> recentWinners = new ArrayList<>();

    public ProfitTracker() {
        this.startTime = System.currentTimeMillis();
    }

    public void addGame(String player, boolean win, long profit) {
        if (win) {
            totalLost += profit; // Bot loses what player wins
            addWinner(player + " (" + formatGP(profit) + ")");
        } else {
            totalWon += Math.abs(profit); // Bot wins what player loses
        }
    }

    public long getNetProfit() {
        return totalWon - totalLost;
    }

    public String getRuntime() {
        long elapsed = System.currentTimeMillis() - startTime;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void addWinner(String entry) {
        recentWinners.add(0, entry);
        if (recentWinners.size() > 5) recentWinners.remove(5);
    }

    public List<String> getRecentWinners() {
        return recentWinners;
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }
}
