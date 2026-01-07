# 🎰 iKingSnipe Titan Casino - DreamBot Script

**Complete casino suite with 13 games for Old School RuneScape via DreamBot**

## 🚀 Quick Start (3 Easy Steps)

### Step 1: Get DreamBot Client

1. Download DreamBot from: **https://dreambot.org/download.php**
2. Install and run DreamBot once
3. The DreamBot client JAR will be in one of these locations:
   - **Windows**: `C:\Users\YourName\.dreambot\cache\`
   - **macOS**: `/Users/YourName/.dreambot/cache/`
   - **Linux**: `/home/yourname/.dreambot/cache/`
4. Look for a file like `client-X.X.X.jar` (e.g., `client-3.1.0.jar`)
5. Copy that JAR file to the `libs/` folder in this project

### Step 2: Build the Script

```bash
# Make sure the DreamBot JAR is in libs/ folder
./gradlew clean build
```

The compiled script will be in: `output/TitanCasino-2.0.0.jar`

### Step 3: Install & Run

1. Copy `output/TitanCasino-2.0.0.jar` to your DreamBot scripts folder:
   - **Windows**: `%USERPROFILE%\.dreambot\scripts\`
   - **macOS/Linux**: `~/.dreambot/scripts/`

2. Open DreamBot
3. Click "Scripts" → Find "iKingSnipe TITAN" → Click "Start"

**That's it! You're running!** 🎉

---

## 📦 What's Included

### Titan Casino Features
- **13 Casino Games**: Dice, Flower Poker, Blackjack, Roulette, and more
- **Admin GUI**: Real-time control panel with statistics
- **Provably Fair**: HMAC-SHA256 RNG verification
- **Player Database**: SQLite persistence
- **Discord Webhooks**: Real-time notifications
- **Anti-Detection**: Human-like behavior patterns
- **Failover System**: Automatic chat fallback
- **Emergency Stop**: Safety mechanisms

---

## 🛠️ Build Requirements

- **Java**: Version 8 or higher
- **DreamBot Client JAR**: Place in `libs/` folder
- **Gradle**: Included (use `./gradlew`)

---

## 📁 Project Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── ikingsnipe.java          # Titan Casino (1,652 lines)
├── libs/
│   └── client-X.X.X.jar         # DreamBot client (you provide)
├── build.gradle                  # Build configuration
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
└── README.md                     # This file
```

---

## 🔧 Detailed Build Instructions

### Option 1: Using Gradle Wrapper (Recommended)

```bash
# Unix/Linux/macOS
./gradlew clean build

# Windows
gradlew.bat clean build
```

### Option 2: Using System Gradle

```bash
gradle clean build
```

### Build Output

After building, you'll find:
- `output/TitanCasino-2.0.0.jar` - Your compiled script

---

## 📝 Configuration

### In-Game Configuration
When you start the script, a GUI will appear where you can configure:
- Clan chat settings
- Game enable/disable
- Bet limits per game
- Discord webhook URL
- Player management

### Code Configuration
Edit `src/main/java/com/ikingsnipe/ikingsnipe.java` to modify:
- Game rules and payouts
- Anti-detection settings
- Database location
- Timing parameters

---

## 🎮 How to Use

1. **Start the script** in DreamBot
2. **Configure** via the GUI that appears
3. **Join clan chat** (if configured)
4. **Players use commands** like:
   - `!dice <amount>` - Roll dice
   - `!fp <amount>` - Flower poker
   - `!bj <amount>` - Blackjack
   - `!help` - Show all commands
5. **Monitor** via the admin GUI

---

## 🔍 Troubleshooting

### "Cannot find DreamBot classes"
**Solution**: Make sure the DreamBot client JAR is in the `libs/` folder

### "Build failed"
**Solution**: 
1. Check Java version: `java -version` (need 8+)
2. Make sure DreamBot JAR is in `libs/`
3. Try: `./gradlew clean build --refresh-dependencies`

### "Script doesn't show in DreamBot"
**Solution**:
1. Make sure JAR is in correct folder (`%USERPROFILE%\.dreambot\scripts\`)
2. Restart DreamBot
3. Click "Refresh" in scripts menu

### "Where is the DreamBot client JAR?"
**Solution**:
1. Run DreamBot once
2. Check `~/.dreambot/cache/` or `C:\Users\YourName\.dreambot\cache\`
3. Look for `client-X.X.X.jar`
4. If not found, download from https://dreambot.org/

---

## 📊 Features Breakdown

### Casino Games (13 Total)
1. **Dice Duel** - Classic dice rolling
2. **Flower Poker** - Flower-based poker
3. **Blackjack** - 21 card game
4. **Roulette** - Wheel spinning
5. **Craps** - Dice game
6. **Slots** - Slot machine
7. **High-Low** - Guess higher/lower
8. **Coin Flip** - Heads or tails
9. **Lucky 7** - Roll a 7 to win
10. **Hot Dice** - Hot/cold dice
11. **55x2** - Double or nothing
12. **Poker Dice** - Poker with dice
13. **Custom** - Configurable games

### Admin Features
- Real-time statistics dashboard
- Player balance management
- Blacklist system
- Game configuration
- Webhook integration
- Emergency controls

### Technical Features
- Provably fair RNG (HMAC-SHA256)
- SQLite database with backups
- Async webhook delivery
- Message queue system
- Anti-detection engine
- Session management
- Error recovery

---

## 🛡️ Security & Anti-Detection

- **Randomized Timing**: All actions have natural delays
- **Human Behavior**: Mouse movements, camera angles
- **Idle Actions**: Periodic anti-ban actions
- **Session Management**: Runtime limits and breaks
- **Pattern Variation**: No predictable behavior

---

## 📜 License

MIT License - See LICENSE file

---

## ⚠️ Disclaimer

**For educational purposes only.**
- Use at your own risk
- No warranty provided
- May violate game terms of service
- Author not responsible for bans

---

## 📧 Support

- **GitHub**: https://github.com/No6love9/ikingsnipe
- **Issues**: https://github.com/No6love9/ikingsnipe/issues

---

## 🎯 Quick Reference

### Build Commands
```bash
./gradlew clean build          # Full clean build
./gradlew build                # Quick build
./gradlew clean                # Clean artifacts
```

### File Locations
```
DreamBot Client: ~/.dreambot/cache/client-X.X.X.jar
DreamBot Scripts: ~/.dreambot/scripts/
Output JAR: output/TitanCasino-2.0.0.jar
```

### Installation Path
```
Windows: %USERPROFILE%\.dreambot\scripts\TitanCasino-2.0.0.jar
macOS/Linux: ~/.dreambot/scripts/TitanCasino-2.0.0.jar
```

---

**Made with ❤️ by ikingsnipe**

**Star ⭐ this repository if you find it useful!**
