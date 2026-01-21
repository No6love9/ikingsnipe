-- iKingSnipe GoatGang Edition - Database Initialization
-- Create the database
CREATE DATABASE IF NOT EXISTS goatgang;
USE goatgang;

-- Players table to track balances and stats
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    balance BIGINT DEFAULT 0,
    total_wagered BIGINT DEFAULT 0,
    total_won BIGINT DEFAULT 0,
    games_played INT DEFAULT 0,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (username)
);

-- Game history for audit and transparency
CREATE TABLE IF NOT EXISTS game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    bet BIGINT NOT NULL,
    result ENUM('WIN', 'LOSS') NOT NULL,
    detail TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES players(username) ON DELETE CASCADE
);

-- Admin logs for security tracking
CREATE TABLE IF NOT EXISTS admin_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_name VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    target_player VARCHAR(255),
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Provably Fair seeds table
CREATE TABLE IF NOT EXISTS game_seeds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_seed VARCHAR(255) NOT NULL,
    client_seed VARCHAR(255),
    nonce INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
