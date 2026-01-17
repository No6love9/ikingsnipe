package com.ikingsnipe.casino.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel {
    private long balance;
    private long xp;
    private int level;
    private long lastDaily;
    private List<Map<String, Object>> auditLog;

    public UserModel() {
        this.balance = 0;
        this.xp = 0;
        this.level = 1;
        this.lastDaily = 0;
        this.auditLog = new ArrayList<>();
    }

    // Getters
    public long getBalance() { return balance; }
    public long getXp() { return xp; }
    public int getLevel() { return level; }
    public long getLastDaily() { return lastDaily; }
    public List<Map<String, Object>> getAuditLog() { return auditLog; }

    // Setters
    public void setBalance(long balance) { this.balance = balance; }
    public void setXp(long xp) { this.xp = xp; }
    public void setLevel(int level) { this.level = level; }
    public void setLastDaily(long lastDaily) { this.lastDaily = lastDaily; }

    // Utility for Audit Log
    public void addAuditEntry(long amount, String reason, long newBalance) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("timestamp", System.currentTimeMillis() / 1000L);
        entry.put("amount", amount);
        entry.put("reason", reason);
        entry.put("new_balance", newBalance);
        this.auditLog.add(entry);
        // Keep log size manageable (last 100 entries)
        if (this.auditLog.size() > 100) {
            this.auditLog = this.auditLog.subList(this.auditLog.size() - 100, this.auditLog.size());
        }
    }
}
