package com.ikingsnipe.casino.models;

public class PlayerSession {
    private final String username;
    private String selectedGame;
    private int betAmount;
    private long sessionStart;

    public PlayerSession(String username) {
        this.username = username;
        this.sessionStart = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public String getSelectedGame() { return selectedGame; }
    public void setSelectedGame(String selectedGame) { this.selectedGame = selectedGame; }
    public int getBetAmount() { return betAmount; }
    public void setBetAmount(int betAmount) { this.betAmount = betAmount; }
    public long getSessionStart() { return sessionStart; }
}
