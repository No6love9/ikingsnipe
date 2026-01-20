#!/bin/bash
# GoatGang Casino Full Server Setup Script

# --- Configuration ---
DB_ROOT_PASSWORD="goatgang_password" # Default password for MySQL root user
DB_NAME="goatgang"
DB_USER="root"
DB_HOST="localhost"

# --- 1. Install MySQL Server ---
echo "--- 1. Installing MySQL Server ---"
sudo apt-get update -qq
sudo apt-get install -y mysql-server

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Set root password (non-interactive)
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$DB_ROOT_PASSWORD';"
sudo mysql -e "FLUSH PRIVILEGES;"

# --- 2. Create Database and Tables ---
echo "--- 2. Creating Database and Tables ---"
# Connect to MySQL and create the database
mysql -u root -p"$DB_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"

# Create the players table
mysql -u root -p"$DB_ROOT_PASSWORD" $DB_NAME -e "
CREATE TABLE IF NOT EXISTS players (
    username VARCHAR(255) PRIMARY KEY,
    balance BIGINT NOT NULL DEFAULT 0,
    total_wagered BIGINT NOT NULL DEFAULT 0,
    total_won BIGINT NOT NULL DEFAULT 0,
    games_played INT NOT NULL DEFAULT 0,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);"

# Create the game_history table
mysql -u root -p"$DB_ROOT_PASSWORD" $DB_NAME -e "
CREATE TABLE IF NOT EXISTS game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    bet BIGINT NOT NULL,
    result VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES players(username)
);"

echo "--- Database setup complete. Root password is: $DB_ROOT_PASSWORD ---"

# --- 3. Finalize Discord Bot Setup ---
echo "--- 3. Finalizing Discord Bot Setup ---"
# Update the Python bot with the DB password
sed -i "s/DB_PASSWORD = \"YOUR_MYSQL_ROOT_PASSWORD\"/DB_PASSWORD = \"$DB_ROOT_PASSWORD\"/" discord_bot/casino_bot.py

echo "--- Setup complete. Please review the setup.sh and discord_bot/casino_bot.py files. ---"
echo "To run the bot, you must first edit discord_bot/casino_bot.py and replace 'YOUR_BOT_TOKEN_HERE' with your actual Discord Bot Token."
echo "Then run: python3 discord_bot/casino_bot.py"
echo "To run the casino script, load the ikingsnipe-14.0.0-GOATGANG.jar in DreamBot."
