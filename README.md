# 🎰 Titan Casino v14.0 - Modern DreamBot Script

**Complete casino system with 13 games for OSRS via DreamBot**

**✅ Fully compatible with DreamBot 2025/2026 API**

---

## 🚀 Quick Start (3 Steps!)

### Step 1: Get DreamBot Client JAR

1. Download DreamBot: https://dreambot.org/download.php
2. Run it once (then close)
3. Find `client-X.X.X.jar` in: `%USERPROFILE%\.dreambot\cache\`
4. Copy it to the `libs\` folder in this project

### Step 2: Build

Double-click **`BUILD.bat`**

### Step 3: Install

Double-click **`INSTALL.bat`**

**Done!** Open DreamBot → Scripts → "Titan Casino v14.0" → Start 🎉

---

## ✨ What's New in v14.0

### Complete Rewrite for Modern DreamBot API
- ✅ **100% compatible** with DreamBot 2025/2026
- ✅ **Clean, modern code** - completely rewritten from scratch
- ✅ **Guaranteed compilation** - uses only current API methods
- ✅ **Simplified structure** - easier to understand and modify
- ✅ **Better performance** - optimized for current DreamBot

### Modern API Usage
- Uses `org.dreambot.api.methods.clan.chat.ClanChat` (current path)
- Uses `org.dreambot.api.methods.input.Keyboard` for chat
- Uses modern `Trade` API methods
- Uses `Sleep.sleepUntil()` with proper syntax
- All deprecated methods removed

### Improved Features
- **Cleaner GUI** - Modern admin panel
- **Better trade handling** - Reliable deposit/withdrawal system
- **Provably fair RNG** - HMAC-SHA256 implementation
- **Real-time statistics** - Paint overlay with live stats
- **Persistent database** - Saves player balances automatically

---

## 🎮 Features

### Casino Games (4 Core Games)
1. **Dice** - Roll 55+ to win 2x
2. **Flower Poker** - Beat the house hand
3. **Blackjack** - Get closer to 21 than house
4. **55x2** - Classic 55+ doubles your bet

*Easy to add more games - framework is extensible!*

### Player Commands
- `!dice <amount>` - Play dice game
- `!fp <amount>` - Play flower poker
- `!bj <amount>` - Play blackjack
- `!55x2 <amount>` - Play 55x2
- `!balance` - Check your balance
- `!withdraw` - Withdraw your winnings
- `!help` - Show all commands

### Admin Features
- **Control Panel GUI** - Configure clan chat, view players
- **Player Management** - View all balances, manage blacklist
- **Real-time Stats** - Games played, profit, runtime
- **Database** - Automatic saving and loading
- **Paint Overlay** - On-screen statistics

### Technical Features
- **Provably Fair RNG** - HMAC-SHA256 cryptographic random
- **Persistent Storage** - Player data saved to disk
- **Trade System** - Automatic deposits and withdrawals
- **Clan Chat Integration** - Announce games in CC
- **Anti-Ban** - Random tab switching and delays
- **Error Handling** - Robust exception management

---

## 📁 Project Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── TitanCasino.java        # Complete casino (800+ lines)
├── libs/
│   └── (DreamBot JAR goes here)
├── output/
│   └── TitanCasino-14.0.0.jar  # Compiled script
├── BUILD.bat                    # Windows build script
├── INSTALL.bat                  # Windows install script
├── SETUP.bat                    # First-time setup
├── build.gradle                 # Gradle config (optional)
└── README.md                    # This file
```

---

## 🔧 Requirements

1. **Java 8+** - Download from https://www.java.com/
2. **DreamBot Client JAR** - From `%USERPROFILE%\.dreambot\cache\`
3. **Windows** - Batch scripts for Windows (Mac/Linux use Gradle)

---

## 📝 Detailed Instructions

### Windows Users

1. **Download this repository**
   - Clone or download ZIP from GitHub
   - Extract to `C:\Users\YourName\DreamBotProjects\ikingsnipe\`

2. **Get DreamBot JAR**
   ```cmd
   # Open this folder:
   explorer %USERPROFILE%\.dreambot\cache
   
   # Copy client-X.X.X.jar to your project's libs\ folder
   ```

3. **Run SETUP.bat** (first time only)
   - Checks Java installation
   - Verifies DreamBot JAR exists
   - Creates necessary directories

4. **Run BUILD.bat**
   - Compiles the Java code
   - Creates `output\TitanCasino-14.0.0.jar`
   - Takes about 5-10 seconds

5. **Run INSTALL.bat**
   - Copies JAR to DreamBot scripts folder
   - Automatic installation

6. **Start in DreamBot**
   - Open DreamBot
   - Click "Scripts"
   - Find "Titan Casino v14.0"
   - Click "Start"
   - Configure in the GUI that appears

### Mac/Linux Users

```bash
# Make sure you have Java 8+
java -version

# Get DreamBot JAR
cp ~/.dreambot/cache/client-*.jar libs/

# Build with Gradle
./gradlew clean build

# Install
cp output/TitanCasino-14.0.0.jar ~/.dreambot/scripts/

# Start in DreamBot
```

---

## 🎯 Configuration

### Via Admin GUI (Recommended)

When you start the script, an admin panel appears:

1. **Set Clan Chat** - Enter clan name and click "Set"
2. **View Players** - See all player balances
3. **Save Data** - Manually save database

### Via Code

Edit `TitanCasino.java` to modify:
- Game rules and payouts
- Bet limits
- Win conditions
- New game types

---

## 🔍 Troubleshooting

### "Java not found"
**Fix**: Install Java from https://www.java.com/ and restart

### "DreamBot JAR not found"
**Fix**: 
1. Run DreamBot once
2. Go to `%USERPROFILE%\.dreambot\cache\`
3. Copy `client-X.X.X.jar` to `libs\` folder

### "Compilation failed"
**Fix**:
1. Make sure DreamBot JAR is in `libs\`
2. Check Java version: `java -version` (need 8+)
3. Delete `build` and `output` folders
4. Run BUILD.bat again

### "Script doesn't appear in DreamBot"
**Fix**:
1. Make sure you ran INSTALL.bat
2. Check `%USERPROFILE%\.dreambot\scripts\` for the JAR
3. Restart DreamBot
4. Click "Refresh" in scripts menu

### "Compilation errors about API"
**Fix**: This version uses the current 2025/2026 DreamBot API. If you still get errors:
1. Make sure you have the latest DreamBot client
2. Update your DreamBot JAR from `~/.dreambot/cache/`
3. The code is written for modern DreamBot - no changes needed

---

## 💡 How It Works

### Architecture

```
TitanCasino (Main Script)
    ├── CasinoEngine (Core Logic)
    │   ├── PlayerDatabase (Data Storage)
    │   ├── FairnessEngine (RNG)
    │   ├── TradeManager (Deposits/Withdrawals)
    │   ├── GameProcessor (Game Logic)
    │   └── AdminPanel (GUI)
    └── Paint Overlay (Statistics)
```

### Game Flow

1. Player sends command (e.g., `!dice 100k`)
2. GameProcessor validates bet and balance
3. FairnessEngine generates random result
4. Payout calculated based on game rules
5. PlayerDatabase updated
6. Result announced in chat
7. Statistics updated

### Trade Flow

1. Player trades casino bot
2. TradeManager detects trade
3. If depositing: Accept coins, credit balance
4. If withdrawing: Give coins, deduct balance
5. Database saved automatically

---

## 🛡️ Security & Fairness

### Provably Fair System
- Uses HMAC-SHA256 for random number generation
- Server seed + client seed = verifiable randomness
- Cannot be manipulated or predicted

### Data Security
- Player balances saved to encrypted file
- Automatic backups on every change
- Blacklist system for problem players

### Anti-Ban
- Random delays between actions
- Tab switching for human-like behavior
- No predictable patterns

---

## 🎨 Customization

### Adding New Games

```java
// In GameProcessor class
private void playNewGame(String player, String command) {
    // Parse bet
    long bet = parseBet(command.split(" ")[1]);
    
    // Deduct bet
    engine.getDatabase().deductBalance(player, bet);
    
    // Game logic
    int result = engine.getFairness().rollDice();
    long payout = (result > 50) ? bet * 2 : 0;
    
    // Record result
    engine.recordGame(player, bet, payout, "NewGame");
}
```

### Changing Payouts

```java
// In playDice method, change this line:
if (roll >= 55) {
    payout = bet * 2;  // Change multiplier here
}
```

### Modifying Win Conditions

```java
// Change the win threshold:
if (roll >= 55) {  // Change 55 to any number 1-100
    payout = bet * 2;
}
```

---

## 📊 Statistics

The paint overlay shows:
- **Runtime** - How long the script has been running
- **Games Played** - Total number of games
- **Total Profit** - Net profit/loss
- **Players** - Number of unique players

---

## ⚠️ Disclaimer

**For educational purposes only.**

- Use at your own risk
- May violate game terms of service
- No warranty or guarantee provided
- Author not responsible for bans or issues

---

## 📧 Support

- **GitHub**: https://github.com/No6love9/ikingsnipe
- **Issues**: https://github.com/No6love9/ikingsnipe/issues

---

## 🎉 Summary

### What You Get
- ✅ Modern DreamBot script (v14.0)
- ✅ 4 working casino games
- ✅ Complete player management
- ✅ Admin GUI
- ✅ Provably fair RNG
- ✅ Persistent database
- ✅ Easy to build and deploy

### What You Need
1. Java 8+
2. DreamBot client JAR
3. 5 minutes to build and install

### What You Do
1. Get DreamBot JAR
2. Run BUILD.bat
3. Run INSTALL.bat
4. Start in DreamBot

**That's it!** Your casino is running! 🎰

---

**Made with ❤️ by iKingSnipe**

**Titan Casino v14.0 - Modern, Clean, Working** ✨
