package com.ikingsnipe.casino.models;

public class PlayerSession {
    private final String username;
    private long owedAmount = 0;
    private long totalWagered = 0;
    private int gamesPlayed = 0;

    public PlayerSession(String username) { this.username = username; }
    public String getUsername() { return username; }
    public long getOwedAmount() { return owedAmount; }
    public void setOwedAmount(long amount) { this.owedAmount = amount; }
    public void addWager(long amount) { this.totalWagered += amount; this.gamesPlayed++; }
    public long getTotalWagered() { return totalWagered; }
    public int getGamesPlayed() { return gamesPlayed; }
}
