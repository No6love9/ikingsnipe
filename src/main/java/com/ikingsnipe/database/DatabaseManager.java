package com.ikingsnipe.database;

import org.dreambot.api.methods.input.Keyboard;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.dreambot.api.utilities.Logger;
import java.sql.*;
import java.util.*;

/**
 * Enterprise Database Manager for GoatGang Edition
 * Handles automated schema setup, connection pooling, and cross-platform compatibility.
 */
public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static boolean useFallback = false;
    private static final Map<String, Long> balanceCache = new HashMap<>();

    /**
     * Initializes the database connection pool and sets up tables.
     */
    public static void setup(String host, String port, String db, String user, String pass) {
        if (host == null || host.isEmpty()) {
            Logger.log("[Database] No host provided. Using in-memory fallback.");
            useFallback = true;
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(5000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            dataSource = new HikariDataSource(config);
            createTables();
            Logger.log("[Database] Connected and initialized successfully.");
        } catch (Exception e) {
            Logger.error("[Database] Setup failed: " + e.getMessage());
            useFallback = true;
        }
    }

    private static void createTables() {
        String playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "username VARCHAR(20) PRIMARY KEY, " +
                "balance BIGINT DEFAULT 0, " +
                "total_wagered BIGINT DEFAULT 0, " +
                "total_won BIGINT DEFAULT 0, " +
                "games_played INT DEFAULT 0, " +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        
        String historyTable = "CREATE TABLE IF NOT EXISTS game_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(20), " +
                "game_type VARCHAR(20), " +
                "bet BIGINT, " +
                "result VARCHAR(10), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(historyTable);
        } catch (SQLException e) {
            Logger.error("[Database] Table creation failed: " + e.getMessage());
        }
    }

    public static long getBalance(String username) {
        if (useFallback) return balanceCache.getOrDefault(username.toLowerCase(), 0L);
        
        String query = "SELECT balance FROM players WHERE username = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("balance");
        } catch (SQLException e) {
            Logger.error("[Database] Error getting balance: " + e.getMessage());
        }
        return 0L;
    }

    public static void updateBalance(String username, long amount) {
        String user = username.toLowerCase();
        if (useFallback) {
            balanceCache.put(user, balanceCache.getOrDefault(user, 0L) + amount);
            return;
        }

        String query = "INSERT INTO players (username, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = balance + ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user);
            stmt.setLong(2, amount);
            stmt.setLong(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("[Database] Error updating balance: " + e.getMessage());
        }
    }

    public static void recordGame(String username, String type, long bet, String result) {
        if (useFallback) return;

        String query = "INSERT INTO game_history (username, game_type, bet, result) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setString(2, type);
            stmt.setLong(3, bet);
            stmt.setString(4, result);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("[Database] Error recording game: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (dataSource != null) dataSource.close();
    }
}
