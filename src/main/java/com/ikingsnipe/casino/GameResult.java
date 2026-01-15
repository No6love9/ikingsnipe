package com.ikingsnipe.casino.games;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for game results with full details
 */
public class GameResult {
    
    private final boolean win;
    private final String message;
    private final int payout;
    private final String gameType;
    private final Map<String, Object> details;
    private final long timestamp;
    
    public GameResult(boolean win, String message, int payout, String gameType) {
        this(win, message, payout, gameType, new HashMap<>());
    }
    
    public GameResult(boolean win, String message, int payout, String gameType, Map<String, Object> details) {
        this.win = win;
        this.message = message;
        this.payout = payout;
        this.gameType = gameType;
        this.details = new HashMap<>(details);
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public boolean isWin() { return win; }
    public String getMessage() { return message; }
    public int getPayout() { return payout; }
    public String getGameType() { return gameType; }
    public Map<String, Object> getDetails() { return new HashMap<>(details); }
    public long getTimestamp() { return timestamp; }
    
    // Utility methods
    public String getSummary() {
        return String.format("[%s] %s | Payout: %,d GP", 
            gameType.toUpperCase(), 
            win ? "WIN" : "LOSS", 
            payout);
    }
    
    public void addDetail(String key, Object value) {
        details.put(key, value);
    }
    
    public Object getDetail(String key) {
        return details.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("GameResult{win=%s, game=%s, payout=%,d, message='%s'}",
            win, gameType, payout, message);
    }
}