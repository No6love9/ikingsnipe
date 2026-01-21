#!/bin/bash

# --- Colors for UI ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# --- Header ---
clear
echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}       iKingSnipe Kali Linux Auto-Setup             ${NC}"
echo -e "${BLUE}====================================================${NC}"
echo -e "${YELLOW}This script will set up proxies, download DreamBot,${NC}"
echo -e "${YELLOW}and configure the environment for iKingSnipe.${NC}"
echo ""

# --- Root Check ---
if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}This script must be run as root (use sudo).${NC}"
   exit 1
fi

# --- Proxy Configuration UI ---
echo -e "${GREEN}[1/4] Proxy Configuration${NC}"
read -p "Do you want to use a proxy for the setup? (y/n): " USE_PROXY

if [[ "$USE_PROXY" =~ ^[Yy]$ ]]; then
    read -p "Enter Proxy Address (e.g., 127.0.0.1): " PROXY_ADDR
    read -p "Enter Proxy Port (e.g., 8080): " PROXY_PORT
    read -p "Proxy Type (http/https/socks5): " PROXY_TYPE
    
    export http_proxy="${PROXY_TYPE}://${PROXY_ADDR}:${PROXY_PORT}"
    export https_proxy="${PROXY_TYPE}://${PROXY_ADDR}:${PROXY_PORT}"
    export ALL_PROXY="${PROXY_TYPE}://${PROXY_ADDR}:${PROXY_PORT}"
    
    echo -e "${YELLOW}Proxy set to: $http_proxy${NC}"
    
    # Configure APT to use proxy
    echo "Acquire::http::Proxy \"${PROXY_TYPE}://${PROXY_ADDR}:${PROXY_PORT}/\";" > /etc/apt/apt.conf.d/99proxy
    echo "Acquire::https::Proxy \"${PROXY_TYPE}://${PROXY_ADDR}:${PROXY_PORT}/\";" >> /etc/apt/apt.conf.d/99proxy
else
    echo -e "${YELLOW}Skipping proxy configuration.${NC}"
fi

# --- System Updates & Dependencies ---
echo ""
echo -e "${GREEN}[2/4] Installing Dependencies${NC}"
apt-get update -qq
apt-get install -y openjdk-11-jdk mysql-server wget curl python3 python3-pip git -qq

# Start MySQL
systemctl start mysql
systemctl enable mysql

# --- DreamBot Download ---
echo ""
echo -e "${GREEN}[3/4] Downloading DreamBot${NC}"
DREAMBOT_URL="https://dreambot.org/DBLauncher.jar"
DREAMBOT_PATH="/home/$SUDO_USER/DreamBot"
mkdir -p "$DREAMBOT_PATH/Scripts"

echo -e "${YELLOW}Downloading DreamBot Launcher via proxy...${NC}"
wget -q --show-progress "$DREAMBOT_URL" -O "$DREAMBOT_PATH/DBLauncher.jar"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}DreamBot downloaded successfully to $DREAMBOT_PATH/DBLauncher.jar${NC}"
else
    echo -e "${RED}Failed to download DreamBot. Please check your proxy settings.${NC}"
fi

# --- iKingSnipe Configuration ---
echo ""
echo -e "${GREEN}[4/4] Configuring iKingSnipe${NC}"

# Database Setup
read -p "Enter MySQL Root Password for iKingSnipe: " DB_PASS
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$DB_PASS';"
mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS goatgang;"

# Run the original database init if it exists
if [ -f "database/init_db.sql" ]; then
    mysql -u root -p"$DB_PASS" goatgang < database/init_db.sql
    echo -e "${YELLOW}Database schema initialized from init_db.sql${NC}"
fi

# Build the project
echo -e "${YELLOW}Building iKingSnipe JAR...${NC}"
chmod +x gradlew
./gradlew clean shadowJar placeJar

# Copy to DreamBot Scripts
cp output/ikingsnipe-14.0.0-GOATGANG.jar "$DREAMBOT_PATH/Scripts/"
cp output/EliteTitanCasino.jar "$DREAMBOT_PATH/Scripts/"

# Fix permissions
chown -R $SUDO_USER:$SUDO_USER "$DREAMBOT_PATH"

# --- Final Summary ---
echo ""
echo -e "${BLUE}====================================================${NC}"
echo -e "${GREEN}             Setup Complete!                        ${NC}"
echo -e "${BLUE}====================================================${NC}"
echo -e "${YELLOW}DreamBot Launcher: $DREAMBOT_PATH/DBLauncher.jar${NC}"
echo -e "${YELLOW}iKingSnipe Scripts: $DREAMBOT_PATH/Scripts/${NC}"
echo -e "${YELLOW}MySQL Password: $DB_PASS${NC}"
echo ""
echo -e "To run DreamBot with proxy, use:"
echo -e "${BLUE}java -Dhttp.proxyHost=$PROXY_ADDR -Dhttp.proxyPort=$PROXY_PORT -jar $DREAMBOT_PATH/DBLauncher.jar${NC}"
echo ""
echo -e "To start the Discord bot, edit discord_bot/casino_bot.py and run:"
echo -e "${BLUE}python3 discord_bot/casino_bot.py${NC}"
echo -e "${BLUE}====================================================${NC}"
