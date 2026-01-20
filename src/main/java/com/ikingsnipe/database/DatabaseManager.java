package com.ikingsnipe.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.dreambot.api.utilities.Logger;

import java.sql.*;
import java.util.*;

/**
 * Database Manager for iKingSnipe Casino
 * Handles player balances, statistics, and transaction history
 */
public class DatabaseManager {
    private HikariDataSource dataSource;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    
    // In-memory fallback if database is unavailable
    private final Map<String, Long> balanceCache = new HashMap<>();
    private boolean useFallback = false;

    public DatabaseManager(String url, String user, String password) {
        this.dbUrl = url;
        this.dbUser = user;
        this.dbPassword = password;
        initialize();
    }

    /**
     * Initialize database connection pool
     */
    private void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            createTablesIfNotExist();
            Logger.log("[DatabaseManager] Connected to database successfully.");
        } catch (Exception e) {
            Logger.log("[DatabaseManager] Failed to connect to database: " + e.getMessage());
            Logger.log("[DatabaseManager] Using in-memory fallback mode.");
            useFallback = true;
        }
    }

    /**
     * Create necessary tables if they don't exist
     */
    private void createTablesIfNotExist() {
        String createPlayersTable = 
            "CREATE TABLE IF NOT EXISTS players (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(255) UNIQUE NOT NULL," +
            "balance BIGINT DEFAULT 0," +
            "total_wagered BIGINT DEFAULT 0," +
            "total_won BIGINT DEFAULT 0," +
            "total_lost BIGINT DEFAULT 0," +
            "games_played INT DEFAULT 0," +
            "games_won INT DEFAULT 0," +
            "games_lost INT DEFAULT 0," +
            "last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        String createTransactionsTable = 
            "CREATE TABLE IF NOT EXISTS transactions (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(255) NOT NULL," +
            "transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'WIN', 'LOSS') NOT NULL," +
            "amount BIGINT NOT NULL," +
            "balance_after BIGINT NOT NULL," +
            "game_type VARCHAR(50)," +
            "seed VARCHAR(255)," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "INDEX idx_username (username)," +
            "INDEX idx_timestamp (timestamp)" +
            ")";
        
        String createGamesTable = 
            "CREATE TABLE IF NOT EXISTS game_history (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(255) NOT NULL," +
            "game_type VARCHAR(50) NOT NULL," +
            "bet_amount BIGINT NOT NULL," +
            "payout_amount BIGINT DEFAULT 0," +
            "result VARCHAR(20) NOT NULL," +
            "seed VARCHAR(255)," +
            "game_data TEXT," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "INDEX idx_username (username)," +
            "INDEX idx_game_type (game_type)" +
            ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createGamesTable);
            Logger.log("[DatabaseManager] Database tables verified/created.");
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error creating tables: " + e.getMessage());
        }
    }

    /**
     * Get database connection from pool
     */
    private Connection getConnection() throws SQLException {
        if (useFallback) {
            throw new SQLException("Database unavailable, using fallback mode");
        }
        return dataSource.getConnection();
    }

    /**
     * Get player balance
     */
    public long getBalance(String username) {
        if (useFallback) {
            return balanceCache.getOrDefault(username.toLowerCase(), 0L);
        }

        String query = "SELECT balance FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("balance");
            }
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error getting balance: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Update player balance
     */
    public void updateBalance(String username, long amount) {
        if (useFallback) {
            String key = username.toLowerCase();
            long current = balanceCache.getOrDefault(key, 0L);
            balanceCache.put(key, current + amount);
            return;
        }

        String query = 
            "INSERT INTO players (username, balance) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE balance = balance + ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setLong(2, amount);
            stmt.setLong(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error updating balance: " + e.getMessage());
        }
    }

    /**
     * Record a transaction
     */
    public void recordTransaction(String username, String type, long amount, long balanceAfter, String gameType, String seed) {
        if (useFallback) {
            return; // Skip transaction logging in fallback mode
        }

        String query = 
            "INSERT INTO transactions (username, transaction_type, amount, balance_after, game_type, seed) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, type);
            stmt.setLong(3, amount);
            stmt.setLong(4, balanceAfter);
            stmt.setString(5, gameType);
            stmt.setString(6, seed);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error recording transaction: " + e.getMessage());
        }
    }

    /**
     * Record game result
     */
    public void recordGame(String username, String gameType, long betAmount, long payoutAmount, String result, String seed, String gameData) {
        if (useFallback) {
            return; // Skip game logging in fallback mode
        }

        String query = 
            "INSERT INTO game_history (username, game_type, bet_amount, payout_amount, result, seed, game_data) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, gameType);
            stmt.setLong(3, betAmount);
            stmt.setLong(4, payoutAmount);
            stmt.setString(5, result);
            stmt.setString(6, seed);
            stmt.setString(7, gameData);
            stmt.executeUpdate();
            
            // Update player statistics
            updatePlayerStats(username, gameType, betAmount, payoutAmount, result);
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error recording game: " + e.getMessage());
        }
    }

    /**
     * Update player statistics
     */
    private void updatePlayerStats(String username, String gameType, long betAmount, long payoutAmount, String result) {
        String query = 
            "INSERT INTO players (username, total_wagered, total_won, total_lost, games_played, games_won, games_lost, last_played) " +
            "VALUES (?, ?, ?, ?, 1, ?, ?, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "total_wagered = total_wagered + ?, " +
            "total_won = total_won + ?, " +
            "total_lost = total_lost + ?, " +
            "games_played = games_played + 1, " +
            "games_won = games_won + ?, " +
            "games_lost = games_lost + ?, " +
            "last_played = NOW()";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            boolean isWin = result.equalsIgnoreCase("WIN");
            long won = isWin ? payoutAmount : 0;
            long lost = isWin ? 0 : betAmount;
            int gamesWon = isWin ? 1 : 0;
            int gamesLost = isWin ? 0 : 1;
            
            stmt.setString(1, username);
            stmt.setLong(2, betAmount);
            stmt.setLong(3, won);
            stmt.setLong(4, lost);
            stmt.setInt(5, gamesWon);
            stmt.setInt(6, gamesLost);
            stmt.setLong(7, betAmount);
            stmt.setLong(8, won);
            stmt.setLong(9, lost);
            stmt.setInt(10, gamesWon);
            stmt.setInt(11, gamesLost);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error updating player stats: " + e.getMessage());
        }
    }

    /**
     * Get player statistics
     */
    public Map<String, Object> getPlayerStats(String username) {
        Map<String, Object> stats = new HashMap<>();
        
        if (useFallback) {
            stats.put("balance", balanceCache.getOrDefault(username.toLowerCase(), 0L));
            stats.put("total_wagered", 0L);
            stats.put("total_won", 0L);
            stats.put("total_lost", 0L);
            stats.put("games_played", 0);
            stats.put("games_won", 0);
            stats.put("games_lost", 0);
            return stats;
        }

        String query = "SELECT * FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("balance", rs.getLong("balance"));
                stats.put("total_wagered", rs.getLong("total_wagered"));
                stats.put("total_won", rs.getLong("total_won"));
                stats.put("total_lost", rs.getLong("total_lost"));
                stats.put("games_played", rs.getInt("games_played"));
                stats.put("games_won", rs.getInt("games_won"));
                stats.put("games_lost", rs.getInt("games_lost"));
            }
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error getting player stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Get top players by profit
     */
    public List<Map<String, Object>> getTopPlayers(int limit) {
        List<Map<String, Object>> topPlayers = new ArrayList<>();
        
        if (useFallback) {
            return topPlayers; // Empty list in fallback mode
        }

        String query = 
            "SELECT username, balance, total_won, total_lost, games_played, games_won " +
            "FROM players " +
            "ORDER BY balance DESC " +
            "LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("username", rs.getString("username"));
                player.put("balance", rs.getLong("balance"));
                player.put("total_won", rs.getLong("total_won"));
                player.put("total_lost", rs.getLong("total_lost"));
                player.put("games_played", rs.getInt("games_played"));
                player.put("games_won", rs.getInt("games_won"));
                topPlayers.add(player);
            }
        } catch (SQLException e) {
            Logger.log("[DatabaseManager] Error getting top players: " + e.getMessage());
        }
        return topPlayers;
    }

    /**
     * Close database connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            Logger.log("[DatabaseManager] Database connection pool closed.");
        }
    }

    /**
     * Check if database is available
     */
    public boolean isAvailable() {
        return !useFallback;
    }
}
