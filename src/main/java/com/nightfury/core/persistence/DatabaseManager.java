package com.nightfury.core.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Persistence Layer: DatabaseManager.java
 * Uses HikariCP and H2 (embedded) for fast, portable persistence.
 * Implements a JsonFallbackProvider for hard-fail recovery.
 */
public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static boolean useFallback = false;
    private static final String DB_URL = "jdbc:h2:./data/snipedb"; // Portable H2 connection string
    private static final String FALLBACK_FILE = "data/balance_fallback.json";
    private static Map<String, Long> balanceCache = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        try {
            // 1. Attempt to connect to H2 Database
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(5000);
            config.setPoolName("iKingSnipe-Pool");

            dataSource = new HikariDataSource(config);
            createTables();
            System.out.println("[DatabaseManager] H2 Database initialized successfully at: " + DB_URL);
            
            // Ensure data directory exists for H2 and JSON fallback
            new java.io.File("data").mkdirs();

        } catch (Exception e) {
            System.err.println("[DatabaseManager] H2 Setup failed. Falling back to JSON persistence: " + e.getMessage());
            useFallback = true;
            loadFallbackData();
        }
    }

    private static void createTables() throws SQLException {
        String playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "username VARCHAR(20) PRIMARY KEY, " +
                "balance BIGINT DEFAULT 0, " +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        String historyTable = "CREATE TABLE IF NOT EXISTS game_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(20), " +
                "action_type VARCHAR(20), " +
                "amount BIGINT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(historyTable);
        }
    }

    // --- Balance Operations ---

    public static long getBalance(String username) {
        String user = username.toLowerCase();
        if (useFallback) return balanceCache.getOrDefault(user, 0L);
        
        String query = "SELECT balance FROM players WHERE username = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("balance");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting balance: " + e.getMessage());
        }
        return 0L;
    }

    public static void updateBalance(String username, long amount) {
        String user = username.toLowerCase();
        if (useFallback) {
            balanceCache.put(user, balanceCache.getOrDefault(user, 0L) + amount);
            saveFallbackData();
            return;
        }

        String query = "MERGE INTO players (username, balance) KEY(username) VALUES (?, getBalance(?, ?) + ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user);
            stmt.setString(2, user);
            stmt.setLong(3, 0L); // Default balance if not found
            stmt.setLong(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error updating balance: " + e.getMessage());
        }
    }

    public static void recordAction(String username, String type, long amount) {
        if (useFallback) return;

        String query = "INSERT INTO game_history (username, action_type, amount) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setString(2, type);
            stmt.setLong(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error recording action: " + e.getMessage());
        }
    }

    // --- JSON Fallback Provider ---

    private static void loadFallbackData() {
        try (FileReader reader = new FileReader(FALLBACK_FILE)) {
            Type type = new TypeToken<Map<String, Long>>() {}.getType();
            balanceCache = GSON.fromJson(reader, type);
            if (balanceCache == null) balanceCache = new HashMap<>();
            System.out.println("[DatabaseManager] Loaded " + balanceCache.size() + " entries from JSON fallback.");
        } catch (IOException e) {
            System.out.println("[DatabaseManager] No existing JSON fallback file found. Starting fresh.");
        }
    }

    private static void saveFallbackData() {
        try (FileWriter writer = new FileWriter(FALLBACK_FILE)) {
            GSON.toJson(balanceCache, writer);
        } catch (IOException e) {
            System.err.println("[DatabaseManager] Error saving JSON fallback data: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (dataSource != null) dataSource.close();
        if (useFallback) saveFallbackData();
    }
}
