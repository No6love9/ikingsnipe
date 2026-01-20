# iKingSnipe Elite Casino - Complete Bot System v11.0

**A professional-grade OSRS casino bot with integrated Discord control, GUI management, and database persistence.**

---

## 🚀 Features

### ✅ Complete Integration
- **DreamBot In-Game Bot** - Fully automated casino operations
- **Discord Bot** - Remote control and monitoring via Discord commands
- **GUI Control Panel** - Real-time monitoring and management interface
- **Database Persistence** - MySQL/MariaDB integration for player tracking
- **Single JAR Package** - All components in one executable file

### 🎮 Casino Games
- **Craps** - 3x payout on winning rolls (7, 9, 12)
- **Dice Duel** - Classic dice rolling game
- **Flower Poker** - Flower poker mechanics
- **Blackjack** - 2.5x payout blackjack
- **Hot/Cold** - Temperature guessing game
- **55x2** - Double or nothing mechanics
- **Dice War** - Competitive dice rolling

### 🛡️ Security & Anti-Ban
- **Humanization Manager** - Random breaks and delays
- **Anti-Scam Protection** - Trade verification system
- **Blacklist System** - Player management and banning
- **Provably Fair** - SHA-256 seed verification
- **VIP System** - Special treatment for trusted players

### 💾 Database Features
- Player balance tracking
- Game history logging
- Session statistics
- Blacklist management
- Configuration persistence
- Analytics and reporting

### 🤖 Discord Bot Commands
- `!start` - Start the in-game bot
- `!stop` - Stop the in-game bot
- `!status` - Get current bot status
- `!stats` - View session statistics
- `!help` - Show available commands

---

## 📋 Requirements

### System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux
- **Java**: JDK 8 or higher (JDK 11 recommended)
- **RAM**: Minimum 2GB, 4GB recommended
- **DreamBot Client**: Latest version

### Database Requirements (Optional)
- **MySQL** 5.7+ or **MariaDB** 10.3+
- Database server running locally or remotely
- Database user with CREATE, INSERT, UPDATE, DELETE, SELECT permissions

### Discord Bot Requirements (Optional)
- Discord bot token from [Discord Developer Portal](https://discord.com/developers/applications)
- Bot invited to your Discord server with appropriate permissions

---

## 🔧 Installation

### Step 1: Database Setup (Optional but Recommended)

1. **Install MySQL/MariaDB**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install mysql-server
   
   # macOS
   brew install mysql
   
   # Windows: Download from https://dev.mysql.com/downloads/mysql/
   ```

2. **Create Database**
   ```sql
   mysql -u root -p
   CREATE DATABASE ikingsnipe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Import Schema**
   ```bash
   mysql -u root -p ikingsnipe < schema.sql
   ```

### Step 2: Discord Bot Setup (Optional)

1. **Create Discord Bot**
   - Go to https://discord.com/developers/applications
   - Click "New Application"
   - Go to "Bot" tab and click "Add Bot"
   - Copy the bot token

2. **Invite Bot to Server**
   - Go to "OAuth2" → "URL Generator"
   - Select scopes: `bot`
   - Select permissions: `Send Messages`, `Read Messages`, `Embed Links`
   - Copy the generated URL and open in browser
   - Select your server and authorize

### Step 3: Configure the Bot

1. **Edit Configuration File**
   ```bash
   nano ikingsnipe_config.properties
   ```

2. **Update Settings**
   ```properties
   # Database
   db.host=localhost
   db.port=3306
   db.database=ikingsnipe
   db.username=root
   db.password=YOUR_PASSWORD
   
   # Discord
   discord.token=YOUR_BOT_TOKEN
   discord.enabled=true
   discord.notification_channel=CHANNEL_ID
   
   # DreamBot
   dreambot.min_bet=1000000
   dreambot.max_bet=2147483647
   dreambot.default_game=craps
   ```

### Step 4: Deploy the Bot

#### Option A: Standalone GUI Application
```bash
java -jar iKingSnipe-Complete-v11.jar
```
This launches the full GUI with control panel for both bots.

#### Option B: DreamBot Script
1. Copy `iKingSnipe-Complete-v11.jar` to your DreamBot scripts folder:
   - **Windows**: `C:\Users\YourName\DreamBot\Scripts\`
   - **macOS/Linux**: `~/DreamBot/Scripts/`
2. Open DreamBot client
3. Select "iKingSnipe Elite Casino" from script selector
4. Configure and start

#### Option C: Headless Mode (Server)
```bash
java -jar iKingSnipe-Complete-v11.jar --headless
```
Runs without GUI for server deployments.

---

## 🎯 Usage Guide

### GUI Control Panel

When you launch the standalone application, you'll see the main control panel with:

1. **Control Buttons**
   - Start/Stop DreamBot
   - Start/Stop Discord Bot
   - Emergency Stop (stops everything)

2. **Status Tab**
   - Current bot states
   - Active player
   - Session runtime
   - Database connection status

3. **Statistics Tab**
   - Session profit/loss
   - Games played
   - Win rate
   - Average profit per game

4. **Logs Tab**
   - Real-time log viewer
   - Color-coded messages
   - Search and filter

5. **Configuration Tab**
   - Edit settings on-the-fly
   - Save configuration
   - Reload configuration

6. **Players Tab**
   - View player database
   - Manage blacklist
   - View player statistics

### DreamBot Script

1. **Start the Script**
   - Load OSRS account in DreamBot
   - Select "iKingSnipe Elite Casino" from scripts
   - Click "Start"

2. **Configure Settings**
   - Select game type
   - Set minimum bet
   - Configure location
   - Enable/disable features

3. **Monitor Performance**
   - Check paint overlay for stats
   - View logs in DreamBot console
   - Monitor Discord notifications

### Discord Bot

1. **Start the Bot**
   - Use GUI control panel, or
   - Run with `--discord-only` flag

2. **Use Commands**
   ```
   !start     - Start in-game bot
   !stop      - Stop in-game bot
   !status    - Check bot status
   !stats     - View statistics
   !help      - Show commands
   ```

3. **Monitor Activity**
   - Bot status updates automatically
   - Game results posted to notification channel
   - Win/loss notifications

---

## ⚙️ Configuration Reference

### Database Settings
| Key | Description | Default |
|-----|-------------|---------|
| `db.host` | Database server hostname | localhost |
| `db.port` | Database server port | 3306 |
| `db.database` | Database name | ikingsnipe |
| `db.username` | Database username | root |
| `db.password` | Database password | (empty) |

### Discord Settings
| Key | Description | Default |
|-----|-------------|---------|
| `discord.token` | Bot token from Discord | (required) |
| `discord.enabled` | Enable Discord bot | false |
| `discord.notification_channel` | Channel ID for notifications | (empty) |
| `discord.admin_role` | Admin role ID | (empty) |

### DreamBot Settings
| Key | Description | Default |
|-----|-------------|---------|
| `dreambot.min_bet` | Minimum bet amount (GP) | 1000000 |
| `dreambot.max_bet` | Maximum bet amount (GP) | 2147483647 |
| `dreambot.default_game` | Default game to play | craps |
| `dreambot.location` | Starting location | GRAND_EXCHANGE |
| `dreambot.auto_bank` | Auto-bank when low | true |
| `dreambot.auto_mule` | Auto-mule when high | false |

### Game Settings
| Key | Description | Default |
|-----|-------------|---------|
| `game.craps.enabled` | Enable Craps game | true |
| `game.craps.multiplier` | Payout multiplier | 3.0 |
| `game.craps.winning_numbers` | Winning numbers | 7,9,12 |

### Humanization Settings
| Key | Description | Default |
|-----|-------------|---------|
| `humanization.enable_breaks` | Enable random breaks | true |
| `humanization.break_frequency` | Break frequency (minutes) | 60 |
| `humanization.break_duration` | Break duration (minutes) | 5 |

---

## 🐛 Troubleshooting

### Database Connection Failed
```
[DatabaseManager] Failed to initialize: Communications link failure
```
**Solution:**
- Verify MySQL/MariaDB is running: `sudo systemctl status mysql`
- Check credentials in `ikingsnipe_config.properties`
- Ensure database exists: `mysql -u root -p -e "SHOW DATABASES;"`
- Check firewall allows port 3306

### Discord Bot Won't Start
```
[DiscordBot] Failed to start: Invalid token
```
**Solution:**
- Verify bot token is correct
- Regenerate token in Discord Developer Portal if needed
- Ensure `discord.enabled=true` in config
- Check bot has proper permissions

### DreamBot Script Not Loading
```
Script not found in selector
```
**Solution:**
- Verify JAR is in correct Scripts folder
- Restart DreamBot client
- Check JAR file size (should be ~14MB)
- Ensure Java 8+ is installed

### GUI Won't Launch
```
Exception in thread "main" java.awt.HeadlessException
```
**Solution:**
- Use `--headless` flag for server environments
- Install X11 server on Linux servers
- Use VNC or remote desktop for GUI access

---

## 📊 Database Schema

### Tables
- **players** - Player accounts and balances
- **game_history** - Complete game transaction log
- **bot_config** - Configuration storage
- **bot_sessions** - Session tracking
- **trade_logs** - Trade history
- **blacklist_history** - Blacklist changes
- **system_logs** - System event logs
- **jackpot** - Jackpot tracking

### Views
- **player_stats** - Aggregated player statistics
- **daily_stats** - Daily performance metrics
- **game_type_stats** - Per-game statistics

### Stored Procedures
- **record_game** - Record a game transaction
- **get_player_stats** - Get player statistics
- **get_top_players** - Get leaderboard

---

## 🔒 Security Best Practices

1. **Database Security**
   - Use strong passwords
   - Create dedicated database user (not root)
   - Restrict remote access if not needed
   - Regular backups

2. **Discord Security**
   - Keep bot token secret
   - Use role-based permissions
   - Limit admin access
   - Monitor bot activity

3. **Bot Security**
   - Use VIP/blacklist system
   - Monitor for suspicious activity
   - Regular log reviews
   - Keep software updated

4. **Account Security**
   - Use dedicated OSRS accounts
   - Enable 2FA on accounts
   - Don't share credentials
   - Use VPN if needed

---

## 📈 Performance Optimization

### Database Optimization
```sql
-- Add indexes for common queries
CREATE INDEX idx_player_timestamp ON game_history(player_id, timestamp DESC);

-- Optimize tables periodically
OPTIMIZE TABLE game_history;
OPTIMIZE TABLE players;
```

### Memory Optimization
```bash
# Increase Java heap size
java -Xmx2G -Xms512M -jar iKingSnipe-Complete-v11.jar
```

### Network Optimization
- Use local database when possible
- Enable connection pooling (already configured)
- Reduce Discord notification frequency
- Use efficient queries

---

## 🆘 Support & Community

### Getting Help
- Check this README first
- Review `ARCHITECTURE.md` for technical details
- Check DreamBot forums
- Review error logs

### Reporting Issues
When reporting issues, include:
- Full error message
- Configuration (redact sensitive info)
- Steps to reproduce
- System information (OS, Java version)

### Contributing
- Fork the repository
- Create feature branch
- Make changes
- Test thoroughly
- Submit pull request

---

## 📜 License & Disclaimer

### License
This software is provided for educational purposes only.

### Disclaimer
- **Use at your own risk**
- Botting violates OSRS Terms of Service
- Account bans are possible
- No warranty or guarantee provided
- Not responsible for any losses

### Legal Notice
This bot is for educational and research purposes. Users are responsible for compliance with all applicable laws and game rules.

---

## 🎓 Credits

**Developed by**: ikingsnipe  
**Version**: 11.0  
**Build Date**: January 2026  
**Java Version**: 8+ (11 recommended)  
**DreamBot API**: 3.x/4.x compatible

### Technologies Used
- **DreamBot API** - OSRS bot framework
- **JDA** - Discord bot library
- **HikariCP** - Database connection pooling
- **MySQL** - Database system
- **Swing** - GUI framework
- **Gradle** - Build system

---

## 🗺️ Roadmap

### Planned Features
- [ ] Web dashboard
- [ ] Mobile app companion
- [ ] Machine learning anti-ban
- [ ] Multi-account support
- [ ] Cloud configuration sync
- [ ] Advanced analytics
- [ ] Custom game plugins
- [ ] Automated testing suite

---

## 📞 Contact

For questions, suggestions, or issues:
- **GitHub**: Create an issue
- **Discord**: Join support server (link in repo)
- **Email**: support@ikingsnipe.com

---

**Thank you for using iKingSnipe Elite Casino!**

*Happy botting! 🎰*
