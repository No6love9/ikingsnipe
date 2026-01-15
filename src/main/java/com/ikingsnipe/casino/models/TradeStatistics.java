package com.ikingsnipe.casino.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trade Statistics Tracker for snipes♧scripts Enterprise
 * Tracks trade performance, player trust levels, and trade history
 */
public class TradeStatistics {
    
    // Overall statistics
    private int totalTradesAttempted = 0;
    private int totalTradesCompleted = 0;
    private int totalTradesDeclined = 0;
    private int totalTradesFailed = 0;
    private long totalValueTraded = 0;
    private long totalPayoutsGiven = 0;
    
    // Anti-scam statistics
    private int scamAttemptsDetected = 0;
    private int valueChangeDetections = 0;
    private int screen2Mismatches = 0;
    
    // Timing statistics
    private long totalTradeTime = 0;
    private long fastestTrade = Long.MAX_VALUE;
    private long slowestTrade = 0;
    
    // Per-player statistics
    private final Map<String, PlayerTradeStats> playerStats = new ConcurrentHashMap<>();
    
    // Recent trade history
    private final LinkedList<TradeRecord> recentTrades = new LinkedList<>();
    private static final int MAX_RECENT_TRADES = 100;
    
    // Session start time
    private final long sessionStartTime;
    
    public TradeStatistics() {
        this.sessionStartTime = System.currentTimeMillis();
    }
    
    /**
     * Record a completed trade
     */
    public void recordCompletedTrade(String playerName, long value, long duration, boolean wasWin) {
        totalTradesCompleted++;
        totalValueTraded += value;
        totalTradeTime += duration;
        
        if (duration < fastestTrade) fastestTrade = duration;
        if (duration > slowestTrade) slowestTrade = duration;
        
        // Update player stats
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordTrade(value, duration, wasWin);
        
        // Add to recent trades
        addRecentTrade(new TradeRecord(playerName, value, duration, TradeOutcome.COMPLETED, wasWin));
    }
    
    /**
     * Record a declined trade
     */
    public void recordDeclinedTrade(String playerName, String reason) {
        totalTradesDeclined++;
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordDecline(reason);
        
        addRecentTrade(new TradeRecord(playerName, 0, 0, TradeOutcome.DECLINED, false));
    }
    
    /**
     * Record a failed trade (error/timeout)
     */
    public void recordFailedTrade(String playerName, String reason) {
        totalTradesFailed++;
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordFailure(reason);
        
        addRecentTrade(new TradeRecord(playerName, 0, 0, TradeOutcome.FAILED, false));
    }
    
    /**
     * Record a trade attempt
     */
    public void recordTradeAttempt(String playerName) {
        totalTradesAttempted++;
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordAttempt();
    }

    public void recordTradeSuccess(String playerName, long value) {
        // Record a successful trade without knowing the game outcome yet
        totalTradesCompleted++;
        totalValueTraded += value;
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordTrade(value, 0, false); // duration 0 for now
    }
    
    /**
     * Record a payout
     */
    public void recordPayout(String playerName, long amount) {
        totalPayoutsGiven += amount;
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordPayout(amount);
    }
    
    /**
     * Record a scam attempt detection
     */
    public void recordScamAttempt(String playerName, String type) {
        scamAttemptsDetected++;
        
        if (type.equals("VALUE_CHANGE")) {
            valueChangeDetections++;
        } else if (type.equals("SCREEN2_MISMATCH")) {
            screen2Mismatches++;
        }
        
        PlayerTradeStats stats = getOrCreatePlayerStats(playerName);
        stats.recordScamAttempt(type);
    }
    
    /**
     * Get or create player stats
     */
    private PlayerTradeStats getOrCreatePlayerStats(String playerName) {
        return playerStats.computeIfAbsent(playerName.toLowerCase(), k -> new PlayerTradeStats(playerName));
    }
    
    /**
     * Get player stats
     */
    public PlayerTradeStats getPlayerStats(String playerName) {
        return playerStats.get(playerName.toLowerCase());
    }
    
    /**
     * Check if player is trusted (has successful trade history)
     */
    public boolean isPlayerTrusted(String playerName, int requiredTrades) {
        PlayerTradeStats stats = playerStats.get(playerName.toLowerCase());
        if (stats == null) return false;
        return stats.getSuccessfulTrades() >= requiredTrades && stats.getScamAttempts() == 0;
    }
    
    /**
     * Get player trust level
     */
    public TrustLevel getPlayerTrustLevel(String playerName) {
        PlayerTradeStats stats = playerStats.get(playerName.toLowerCase());
        if (stats == null) return TrustLevel.NEW;
        
        if (stats.getScamAttempts() > 0) return TrustLevel.SUSPICIOUS;
        if (stats.getSuccessfulTrades() >= 10) return TrustLevel.TRUSTED;
        if (stats.getSuccessfulTrades() >= 3) return TrustLevel.KNOWN;
        return TrustLevel.NEW;
    }
    
    /**
     * Add to recent trades
     */
    private void addRecentTrade(TradeRecord record) {
        synchronized (recentTrades) {
            recentTrades.addFirst(record);
            while (recentTrades.size() > MAX_RECENT_TRADES) {
                recentTrades.removeLast();
            }
        }
    }
    
    /**
     * Get recent trades
     */
    public List<TradeRecord> getRecentTrades(int count) {
        synchronized (recentTrades) {
            return new ArrayList<>(recentTrades.subList(0, Math.min(count, recentTrades.size())));
        }
    }
    
    // Getters for overall statistics
    public int getTotalTradesAttempted() { return totalTradesAttempted; }
    public int getTotalTradesCompleted() { return totalTradesCompleted; }
    public int getTotalTradesDeclined() { return totalTradesDeclined; }
    public int getTotalTradesFailed() { return totalTradesFailed; }
    public long getTotalValueTraded() { return totalValueTraded; }
    public long getTotalPayoutsGiven() { return totalPayoutsGiven; }
    public int getScamAttemptsDetected() { return scamAttemptsDetected; }
    
    public double getTradeSuccessRate() {
        if (totalTradesAttempted == 0) return 0;
        return (double) totalTradesCompleted / totalTradesAttempted * 100;
    }
    
    public long getAverageTradeTime() {
        if (totalTradesCompleted == 0) return 0;
        return totalTradeTime / totalTradesCompleted;
    }
    
    public long getFastestTrade() { return fastestTrade == Long.MAX_VALUE ? 0 : fastestTrade; }
    public long getSlowestTrade() { return slowestTrade; }
    
    public long getSessionDuration() {
        return System.currentTimeMillis() - sessionStartTime;
    }
    
    public int getUniquePlayersTraded() {
        return playerStats.size();
    }
    
    /**
     * Player-specific trade statistics
     */
    public static class PlayerTradeStats {
        private final String playerName;
        private int tradeAttempts = 0;
        private int successfulTrades = 0;
        private int declinedTrades = 0;
        private int failedTrades = 0;
        private long totalWagered = 0;
        private long totalPaidOut = 0;
        private int wins = 0;
        private int losses = 0;
        private int scamAttempts = 0;
        private long totalTradeTime = 0;
        private long firstTradeTime = 0;
        private long lastTradeTime = 0;
        private final List<String> declineReasons = new ArrayList<>();
        private final List<String> scamTypes = new ArrayList<>();
        
        public PlayerTradeStats(String playerName) {
            this.playerName = playerName;
        }
        
        public void recordAttempt() {
            tradeAttempts++;
            if (firstTradeTime == 0) firstTradeTime = System.currentTimeMillis();
        }
        
        public void recordTrade(long value, long duration, boolean wasWin) {
            successfulTrades++;
            totalWagered += value;
            totalTradeTime += duration;
            lastTradeTime = System.currentTimeMillis();
            if (wasWin) wins++; else losses++;
        }
        
        public void recordDecline(String reason) {
            declinedTrades++;
            if (reason != null) declineReasons.add(reason);
        }
        
        public void recordFailure(String reason) {
            failedTrades++;
        }
        
        public void recordPayout(long amount) {
            totalPaidOut += amount;
        }
        
        public void recordScamAttempt(String type) {
            scamAttempts++;
            if (type != null) scamTypes.add(type);
        }
        
        // Getters
        public String getPlayerName() { return playerName; }
        public int getTradeAttempts() { return tradeAttempts; }
        public int getSuccessfulTrades() { return successfulTrades; }
        public int getDeclinedTrades() { return declinedTrades; }
        public int getFailedTrades() { return failedTrades; }
        public long getTotalWagered() { return totalWagered; }
        public long getTotalPaidOut() { return totalPaidOut; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getScamAttempts() { return scamAttempts; }
        public long getTotalTradeTime() { return totalTradeTime; }
        public long getFirstTradeTime() { return firstTradeTime; }
        public long getLastTradeTime() { return lastTradeTime; }
        
        public double getWinRate() {
            int total = wins + losses;
            if (total == 0) return 0;
            return (double) wins / total * 100;
        }
        
        public long getNetProfit() {
            return totalWagered - totalPaidOut;
        }
    }
    
    /**
     * Trade record for history
     */
    public static class TradeRecord {
        public final String playerName;
        public final long value;
        public final long duration;
        public final TradeOutcome outcome;
        public final boolean wasWin;
        public final long timestamp;
        
        public TradeRecord(String playerName, long value, long duration, TradeOutcome outcome, boolean wasWin) {
            this.playerName = playerName;
            this.value = value;
            this.duration = duration;
            this.outcome = outcome;
            this.wasWin = wasWin;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Trade outcome types
     */
    public enum TradeOutcome {
        COMPLETED,
        DECLINED,
        FAILED,
        CANCELLED
    }
    
    /**
     * Player trust levels
     */
    public enum TrustLevel {
        NEW("New Player"),
        KNOWN("Known Player"),
        TRUSTED("Trusted Player"),
        SUSPICIOUS("Suspicious Player");
        
        private final String displayName;
        
        TrustLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
