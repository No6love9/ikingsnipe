# iKingSnipe Elite Casino - Quick Start Guide

Get up and running in 5 minutes!

---

## 🚀 Fastest Setup (No Database, No Discord)

### 1. Install Java
```bash
# Check if Java is installed
java -version

# If not, install Java 11
# Ubuntu/Debian:
sudo apt-get install openjdk-11-jdk

# macOS:
brew install openjdk@11

# Windows: Download from https://adoptium.net/
```

### 2. Run the Bot
```bash
java -jar iKingSnipe-Complete-v11.jar
```

### 3. Use in DreamBot
1. Copy `iKingSnipe-Complete-v11.jar` to:
   - **Windows**: `C:\Users\YourName\DreamBot\Scripts\`
   - **Mac/Linux**: `~/DreamBot/Scripts/`
2. Open DreamBot
3. Select "iKingSnipe Elite Casino" from scripts
4. Click Start

**That's it!** The bot will run with default settings.

---

## 💾 With Database (Recommended)

### 1. Install MySQL
```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# macOS
brew install mysql
brew services start mysql

# Windows: Download from https://dev.mysql.com/downloads/mysql/
```

### 2. Create Database
```bash
mysql -u root -p
```
```sql
CREATE DATABASE ikingsnipe;
exit;
```

### 3. Import Schema
```bash
mysql -u root -p ikingsnipe < schema.sql
```

### 4. Configure Bot
Edit `ikingsnipe_config.properties`:
```properties
db.host=localhost
db.database=ikingsnipe
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 5. Run
```bash
java -jar iKingSnipe-Complete-v11.jar
```

---

## 🤖 With Discord Bot

### 1. Create Discord Bot
1. Go to https://discord.com/developers/applications
2. Click "New Application"
3. Name it "iKingSnipe Casino"
4. Go to "Bot" tab → "Add Bot"
5. Copy the token

### 2. Invite to Server
1. Go to "OAuth2" → "URL Generator"
2. Check: `bot`
3. Check: `Send Messages`, `Read Messages`, `Embed Links`
4. Copy URL and open in browser
5. Select your server

### 3. Configure Bot
Edit `ikingsnipe_config.properties`:
```properties
discord.token=YOUR_BOT_TOKEN_HERE
discord.enabled=true
discord.notification_channel=YOUR_CHANNEL_ID
```

### 4. Run
```bash
java -jar iKingSnipe-Complete-v11.jar
```

### 5. Test Commands
In Discord:
```
!help
!status
!start
```

---

## 🎮 Basic Configuration

### Minimum Settings
```properties
# Betting
dreambot.min_bet=1000000
dreambot.max_bet=2147483647

# Game
dreambot.default_game=craps

# Location
dreambot.location=GRAND_EXCHANGE
```

### Advertising Messages
```properties
advertising.message_1=🎰 iKingSnipe Casino | Craps 3x Payout | Trade me!
advertising.message_2=💰 Trusted Casino Host | Instant Trades | Fair Games!
advertising.interval_seconds=30
```

---

## 📊 GUI Overview

### Main Tabs
1. **Status** - Current bot state, player, runtime
2. **Statistics** - Profit, games played, win rate
3. **Logs** - Real-time log viewer
4. **Configuration** - Edit settings
5. **Players** - Player management

### Control Buttons
- **Start DreamBot** - Start in-game bot
- **Stop DreamBot** - Stop in-game bot
- **Start Discord Bot** - Start Discord bot
- **Stop Discord Bot** - Stop Discord bot
- **Emergency Stop** - Stop everything immediately

---

## 🎯 Common Tasks

### Change Game Type
```properties
dreambot.default_game=craps
# Options: craps, dice, flower, blackjack, hotcold, 55x2, dicewar
```

### Adjust Bet Limits
```properties
dreambot.min_bet=5000000      # 5M minimum
dreambot.max_bet=100000000    # 100M maximum
```

### Enable Auto-Banking
```properties
dreambot.auto_bank=true
dreambot.restock_threshold=10000000
dreambot.restock_amount=100000000
```

### Enable Humanization
```properties
humanization.enable_breaks=true
humanization.break_frequency=60    # Every 60 minutes
humanization.break_duration=5      # 5 minute breaks
```

---

## 🐛 Quick Troubleshooting

### Bot Won't Start
```bash
# Check Java version
java -version

# Should be 8 or higher
```

### Database Connection Failed
```bash
# Check MySQL is running
sudo systemctl status mysql

# Or on macOS:
brew services list
```

### Discord Bot Not Responding
1. Check token is correct
2. Verify bot is in server
3. Check `discord.enabled=true`
4. Ensure bot has permissions

### Script Not in DreamBot
1. Check JAR is in Scripts folder
2. Restart DreamBot
3. Verify file size (~14MB)

---

## 📈 Performance Tips

### Optimize Database
```sql
-- Run in MySQL
OPTIMIZE TABLE game_history;
OPTIMIZE TABLE players;
```

### Increase Memory
```bash
java -Xmx2G -jar iKingSnipe-Complete-v11.jar
```

### Reduce Logging
```properties
logging.level=WARN
```

---

## 🔒 Security Checklist

- [ ] Use strong database password
- [ ] Keep Discord token secret
- [ ] Enable blacklist system
- [ ] Use dedicated OSRS account
- [ ] Regular backups
- [ ] Monitor logs for suspicious activity

---

## 📞 Need Help?

1. **Read** `README_COMPLETE.md` for full documentation
2. **Check** `ARCHITECTURE.md` for technical details
3. **Review** error logs in GUI or console
4. **Search** DreamBot forums
5. **Ask** in Discord support server

---

## 🎓 Next Steps

Once you're comfortable with basic setup:

1. **Explore Advanced Features**
   - Multi-game support
   - VIP system
   - Custom messages
   - Analytics

2. **Optimize Performance**
   - Database tuning
   - Memory allocation
   - Network optimization

3. **Customize**
   - Edit game rules
   - Create custom responses
   - Adjust humanization

4. **Monitor**
   - Check statistics regularly
   - Review player activity
   - Analyze profitability

---

**You're all set! Happy botting! 🎰**

For detailed information, see `README_COMPLETE.md`
