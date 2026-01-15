package com.ikingsnipe.casino.models;
public class PlayerSession {
    private final String username; private long owedAmount = 0;
    public PlayerSession(String username) { this.username = username; }
    public String getUsername() { return username; }
    public long getOwedAmount() { return owedAmount; }
    public void setOwedAmount(long owedAmount) { this.owedAmount = owedAmount; }
}
