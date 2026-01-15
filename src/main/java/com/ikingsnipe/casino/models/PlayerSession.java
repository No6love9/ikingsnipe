package com.ikingsnipe.casino.models;

public class PlayerSession {
    private final String username;
    private long balance = 0; // Current balance the player can bet with
    private long owedAmount = 0; // Amount the bot needs to pay out physically
    private long totalWagered = 0;
    private int gamesPlayed = 0;

    public PlayerSession(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void addBalance(long amount) {
        this.balance += amount;
    }

    public void subtractBalance(long amount) {
        this.balance -= amount;
    }

    public long getOwedAmount() {
        return owedAmount;
    }

    public void setOwedAmount(long amount) {
        this.owedAmount = amount;
    }

    public void addWager(long amount) {
        this.totalWagered += amount;
        this.gamesPlayed++;
    }

    public long getTotalWagered() {
        return totalWagered;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addGame(com.ikingsnipe.casino.games.GameResult result) {
        this.gamesPlayed++;
        if (result.isWin()) {
            this.balance += result.getPayout();
        }
    }
}
