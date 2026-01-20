-- iKingSnipe Elite Casino Database Schema
-- MySQL/MariaDB compatible
-- Version: 11.0

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ikingsnipe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ikingsnipe;

-- ========== Players Table ==========
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    discord_id VARCHAR(50) DEFAULT NULL,
    balance_gp BIGINT DEFAULT 0,
    total_wagered BIGINT DEFAULT 0,
    total_won BIGINT DEFAULT 0,
    total_lost BIGINT DEFAULT 0,
    games_played INT DEFAULT 0,
    is_vip BOOLEAN DEFAULT FALSE,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_discord_id (discord_id),
    INDEX idx_blacklisted (is_blacklisted),
    INDEX idx_vip (is_vip),
    INDEX idx_last_seen (last_seen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Game History Table ==========
CREATE TABLE IF NOT EXISTS game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    bet_amount BIGINT NOT NULL,
    payout_amount BIGINT DEFAULT 0,
    result VARCHAR(20) NOT NULL,
    seed_hash VARCHAR(64) DEFAULT NULL,
    game_data JSON DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_game_type (game_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Bot Configuration Table ==========
CREATE TABLE IF NOT EXISTS bot_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT DEFAULT NULL,
    config_type VARCHAR(20) DEFAULT 'string',
    description TEXT DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Bot Sessions Table ==========
CREATE TABLE IF NOT EXISTS bot_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL DEFAULT NULL,
    total_profit BIGINT DEFAULT 0,
    games_played INT DEFAULT 0,
    runtime_minutes INT DEFAULT 0,
    session_notes TEXT DEFAULT NULL,
    
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Trade Logs Table ==========
CREATE TABLE IF NOT EXISTS trade_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    trade_type VARCHAR(20) NOT NULL,
    amount_gp BIGINT NOT NULL,
    items_traded JSON DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'completed',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_trade_type (trade_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Blacklist History Table ==========
CREATE TABLE IF NOT EXISTS blacklist_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    reason TEXT DEFAULT NULL,
    admin_name VARCHAR(50) DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== System Logs Table ==========
CREATE TABLE IF NOT EXISTS system_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    log_level VARCHAR(20) NOT NULL,
    log_message TEXT NOT NULL,
    log_source VARCHAR(100) DEFAULT NULL,
    log_data JSON DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_log_level (log_level),
    INDEX idx_timestamp (timestamp),
    INDEX idx_log_source (log_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Jackpot Table ==========
CREATE TABLE IF NOT EXISTS jackpot (
    id INT AUTO_INCREMENT PRIMARY KEY,
    current_amount BIGINT DEFAULT 0,
    last_winner_id INT DEFAULT NULL,
    last_win_amount BIGINT DEFAULT 0,
    last_win_timestamp TIMESTAMP NULL DEFAULT NULL,
    total_contributed BIGINT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (last_winner_id) REFERENCES players(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial jackpot record
INSERT INTO jackpot (current_amount, total_contributed) VALUES (0, 0)
ON DUPLICATE KEY UPDATE current_amount = current_amount;

-- ========== Views for Analytics ==========

-- Player statistics view
CREATE OR REPLACE VIEW player_stats AS
SELECT 
    p.id,
    p.username,
    p.discord_id,
    p.balance_gp,
    p.total_wagered,
    p.total_won,
    p.total_lost,
    p.games_played,
    (p.total_won - p.total_lost) AS net_profit,
    CASE 
        WHEN p.total_wagered > 0 THEN (p.total_won / p.total_wagered * 100)
        ELSE 0 
    END AS win_rate_percent,
    p.is_vip,
    p.is_blacklisted,
    p.created_at,
    p.last_seen
FROM players p;

-- Daily statistics view
CREATE OR REPLACE VIEW daily_stats AS
SELECT 
    DATE(gh.timestamp) AS date,
    COUNT(*) AS total_games,
    SUM(CASE WHEN gh.result = 'WIN' THEN 1 ELSE 0 END) AS total_wins,
    SUM(CASE WHEN gh.result = 'LOSS' THEN 1 ELSE 0 END) AS total_losses,
    SUM(gh.bet_amount) AS total_wagered,
    SUM(gh.payout_amount) AS total_paid_out,
    SUM(gh.bet_amount - gh.payout_amount) AS house_profit
FROM game_history gh
GROUP BY DATE(gh.timestamp)
ORDER BY date DESC;

-- Game type statistics view
CREATE OR REPLACE VIEW game_type_stats AS
SELECT 
    gh.game_type,
    COUNT(*) AS total_games,
    SUM(CASE WHEN gh.result = 'WIN' THEN 1 ELSE 0 END) AS total_wins,
    SUM(CASE WHEN gh.result = 'LOSS' THEN 1 ELSE 0 END) AS total_losses,
    SUM(gh.bet_amount) AS total_wagered,
    SUM(gh.payout_amount) AS total_paid_out,
    SUM(gh.bet_amount - gh.payout_amount) AS house_profit,
    AVG(gh.bet_amount) AS avg_bet
FROM game_history gh
GROUP BY gh.game_type;

-- ========== Stored Procedures ==========

DELIMITER //

-- Procedure to record a game
CREATE PROCEDURE IF NOT EXISTS record_game(
    IN p_username VARCHAR(50),
    IN p_game_type VARCHAR(50),
    IN p_bet_amount BIGINT,
    IN p_payout_amount BIGINT,
    IN p_result VARCHAR(20),
    IN p_seed_hash VARCHAR(64)
)
BEGIN
    DECLARE v_player_id INT;
    
    -- Get or create player
    SELECT id INTO v_player_id FROM players WHERE username = p_username;
    
    IF v_player_id IS NULL THEN
        INSERT INTO players (username) VALUES (p_username);
        SET v_player_id = LAST_INSERT_ID();
    END IF;
    
    -- Insert game history
    INSERT INTO game_history (player_id, game_type, bet_amount, payout_amount, result, seed_hash)
    VALUES (v_player_id, p_game_type, p_bet_amount, p_payout_amount, p_result, p_seed_hash);
    
    -- Update player statistics
    IF p_result = 'WIN' THEN
        UPDATE players 
        SET total_wagered = total_wagered + p_bet_amount,
            total_won = total_won + p_payout_amount,
            games_played = games_played + 1,
            last_seen = CURRENT_TIMESTAMP
        WHERE id = v_player_id;
    ELSE
        UPDATE players 
        SET total_wagered = total_wagered + p_bet_amount,
            total_lost = total_lost + p_bet_amount,
            games_played = games_played + 1,
            last_seen = CURRENT_TIMESTAMP
        WHERE id = v_player_id;
    END IF;
END //

-- Procedure to get player statistics
CREATE PROCEDURE IF NOT EXISTS get_player_stats(IN p_username VARCHAR(50))
BEGIN
    SELECT * FROM player_stats WHERE username = p_username;
END //

-- Procedure to get top players
CREATE PROCEDURE IF NOT EXISTS get_top_players(IN p_limit INT)
BEGIN
    SELECT * FROM player_stats 
    ORDER BY net_profit DESC 
    LIMIT p_limit;
END //

DELIMITER ;

-- ========== Sample Data (Optional) ==========

-- Insert sample configuration
INSERT INTO bot_config (config_key, config_value, config_type, description) VALUES
('min_bet', '1000000', 'long', 'Minimum bet amount in GP'),
('max_bet', '2147483647', 'long', 'Maximum bet amount in GP'),
('default_game', 'craps', 'string', 'Default game to play'),
('jackpot_enabled', 'true', 'boolean', 'Enable jackpot feature'),
('jackpot_contribution', '1.0', 'double', 'Jackpot contribution percentage')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- ========== Indexes for Performance ==========

-- Additional composite indexes for common queries
CREATE INDEX idx_player_game_history ON game_history(player_id, timestamp DESC);
CREATE INDEX idx_game_result_timestamp ON game_history(result, timestamp DESC);

-- ========== Database Maintenance ==========

-- Event to clean old logs (runs daily)
CREATE EVENT IF NOT EXISTS clean_old_logs
ON SCHEDULE EVERY 1 DAY
DO
DELETE FROM system_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ========== Completion Message ==========
SELECT 'Database schema created successfully!' AS status;
