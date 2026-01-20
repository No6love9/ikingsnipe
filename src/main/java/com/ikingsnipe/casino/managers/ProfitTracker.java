package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.input.Keyboard;


import java.util.ArrayList;
import java.util.List;

public class ProfitTracker {
    private long totalWon = 0;
    private long totalLost = 0;
    private long totalWagered = 0;
    private long totalPaidOut = 0;
    private long startTime;
    private final List<String> recentWinners = new ArrayList<>();

    public ProfitTracker() {
        this.startTime = System.currentTimeMillis();
    }

    public void addGame(String player, boolean win, long wager, long payout) {
        totalWagered += wager;
        if (win) {
            totalLost += (payout - wager);
            totalPaidOut += payout;
            addWinner(player + " (" + formatGP(payout) + ")");
        } else {
            totalWon += wager;
        }
    }

    public long getTotalWagered() { return totalWagered; }
    public long getTotalPaidOut() { return totalPaidOut; }

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

    public void recordWin(long amount) {
        totalWon += amount;
    }

    public void recordLoss(long amount) {
        totalLost += amount;
    }

    public long getTotalProfit() {
        return getNetProfit();
    }

    public int getTotalWins() {
        return (int)(totalWon / 1000000); // Approximate count
    }

    public int getTotalLosses() {
        return (int)(totalLost / 1000000); // Approximate count
    }

    /**
     * Records a game result for profit tracking
     */
    public static void recordGame(boolean win, long betAmount) {
        // Static method for easy access from BotApplication
        // In a real implementation, this would update a singleton instance
        // For now, we'll just log it
        org.dreambot.api.utilities.Logger.log(String.format("[ProfitTracker] Game recorded: %s, Bet: %s", 
            win ? "WIN" : "LOSS", BalanceManager.formatGP(betAmount)));
    }
}
