package com.nightfury.core.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Persistence Layer: DatabaseManager.java
 * Refactored for Java 8/11 and DreamBot compatibility.
 */
public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static boolean useFallback = false;
    private static final String DB_URL = "jdbc:h2:./data/snipedb;MODE=MySQL"; 
    private static final String FALLBACK_FILE = "data/balance_fallback.json";
    private static Map<String, Long> balanceCache = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        try {
            new File("data").mkdirs();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(5000);

            dataSource = new HikariDataSource(config);
            createTables();
            System.out.println("[DatabaseManager] H2 Database initialized successfully.");

        } catch (Exception e) {
            System.err.println("[DatabaseManager] H2 Setup failed. Using JSON: " + e.getMessage());
            useFallback = true;
            loadFallbackData();
        }
    }

    private static void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "username VARCHAR(20) PRIMARY KEY, " +
                    "balance BIGINT DEFAULT 0, " +
                    "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS game_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(20), " +
                    "action_type VARCHAR(20), " +
                    "amount BIGINT, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    public static long getBalance(String username) {
        String user = username.toLowerCase();
        if (useFallback) return balanceCache.getOrDefault(user, 0L);
        
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM players WHERE username = ?")) {
            stmt.setString(1, user);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("balance");
            }
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

        try (Connection conn = dataSource.getConnection()) {
            long current = getBalance(user);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO players (username, balance) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE balance = ?")) {
                stmt.setString(1, user);
                stmt.setLong(2, current + amount);
                stmt.setLong(3, current + amount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error updating balance: " + e.getMessage());
        }
    }

    private static void loadFallbackData() {
        File file = new File(FALLBACK_FILE);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Long>>() {}.getType();
            balanceCache = GSON.fromJson(reader, type);
            if (balanceCache == null) balanceCache = new HashMap<>();
        } catch (IOException e) {
            System.err.println("[DatabaseManager] Fallback load error: " + e.getMessage());
        }
    }

    private static void saveFallbackData() {
        try (FileWriter writer = new FileWriter(FALLBACK_FILE)) {
            GSON.toJson(balanceCache, writer);
        } catch (IOException e) {
            System.err.println("[DatabaseManager] Fallback save error: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (dataSource != null) dataSource.close();
        if (useFallback) saveFallbackData();
    }
}
