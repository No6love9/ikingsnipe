# Elite Titan Casino - Full Implementation

A professional-grade OSRS casino bot for DreamBot 3/4, featuring complete logic for **Dice, Wheel, Roulette, and Craps** games.

## 🎮 Features

### Game Modes
- **Dice** - Classic dice rolling game
- **Wheel** - Spinning wheel of fortune
- **Roulette** - OSRS roulette table
- **🎲 Craps** - NEW! Provably fair dice game with 3x payouts

### Core Features
- ✅ **Full Game Logic**: Complete interaction steps for all games
- ✅ **Trade Safety**: Automatic verification of trade amounts and safe acceptance
- ✅ **Configurable GUI**: Set your game, bet amount, and custom messages on startup
- ✅ **Auto-Advertising**: Built-in chat automation to attract players
- ✅ **Session Recovery**: Handles logins and state resets automatically
- ✅ **Real-time Stats**: Paint overlay showing wins, losses, and profit
- ✅ **Provably Fair RNG**: Craps game uses cryptographic seed verification

## 🎲 Craps Game Highlights

The **Craps game** is a fully-featured addition with professional implementation:

### Game Rules
- **Win**: Roll 7, 9, or 12 → **3x payout**
- **Loss**: Any other total
- **Provably Fair**: SHA-256 seed hashing for transparency
- **Double or Nothing**: Optional feature to re-bet winnings

### Technical Features
- Secure RNG with player-specific seeds
- Full verification logging
- State machine for reliable execution
- DreamBot API 4 compatible

📖 **[Read Full Craps Game Guide](CRAPS_GAME_GUIDE.md)**

## 📁 Project Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── EliteTitanCasino.java       # Core script logic v7.0 (Fully Implemented)
├── libs/
│   └── dreambot-api.jar            # DreamBot 3/4 API dependency
├── output/
│   └── EliteTitanCasino.jar        # Compiled JAR (17 MB)
├── build.gradle                    # Gradle build configuration
├── CRAPS_GAME_GUIDE.md             # Detailed Craps game documentation
└── README.md                       # This file
```

## 🚀 Setup Instructions

### Prerequisites
- **Java JDK 11** or higher
- **DreamBot Client** installed
- **OSRS Account** with casino access

### Method 1: Quick Start (Pre-compiled)
1. Download `EliteTitanCasino.jar` from `output/` directory
2. Copy to your DreamBot scripts folder:
   - Windows: `C:\Users\YourName\DreamBot\Scripts\`
   - Mac/Linux: `~/DreamBot/Scripts/`
3. Open DreamBot client and log in to OSRS
4. Start the script from the script manager
5. Configure settings in the popup GUI
6. Click **Start Script**

### Method 2: Build from Source

#### Linux/Mac
```bash
cd ikingsnipe
./gradlew clean build
```

#### Windows
```batch
BUILD.bat
```

The compiled JAR will be in `output/EliteTitanCasino.jar`

## ⚙️ Configuration

When you start the script, a GUI will appear with these options:

| Setting | Description | Default |
|---------|-------------|---------|
| **Select Game** | Choose: Dice, Wheel, Roulette, or Craps | Dice |
| **Bet Amount** | Minimum bet amount in GP | 1,000,000 |
| **Min Trade** | Minimum trade to accept | 1,000,000 |
| **Ad Message** | Custom advertising message | "Elite Casino \| Fast Payouts..." |
| **Win Message** | Message sent when player wins | "Congratulations! You won!" |
| **Loss Message** | Message sent when player loses | "Better luck next time!" |
| **Auto Accept Trades** | Automatically accept valid trades | ✅ Enabled |
| **Enable Double or Nothing** | Offer re-bet after Craps wins | ✅ Enabled |

## 🎯 How to Use

### Step 1: Start Script
1. Load EliteTitanCasino in DreamBot
2. Configure your preferred game and settings
3. Click "Start Script"

### Step 2: Advertising
The bot will automatically advertise in chat:
```
Elite Casino | Fast Payouts | Dice, Wheel, Roulette, Craps!
```

### Step 3: Accept Trades
- Players trade you their bet amount
- Bot validates amount >= minimum bet
- Trade accepted automatically (if enabled)

### Step 4: Play Game
- Bot interacts with game objects
- Rolls dice, spins wheel, or plays roulette
- For Craps: Announces dice roll and results

### Step 5: Payout
- Winners receive payouts automatically
- Results announced in chat
- Stats updated on paint overlay

## 🎲 Craps Game Example

```
Player trades 10M GP
🎲 Rolling craps for PlayerName... Bet: 10,000,000 GP
🎲 Dice: 4 + 3 = 7
🎉 PlayerName WINS! Payout: 30,000,000 GP (3x)
💰 Double or Nothing? Trade me 30,000,000 GP to roll again!
🔐 Verify: a3f5d8e2c1b4f6a9...
```

## 📊 Paint Overlay

Real-time information displayed on screen:
```
Elite Titan Casino v7.0
State: IDLE
Game: Craps
Wins: 15 | Losses: 42
Current Player: PlayerName
Current Bet: 10,000,000 GP
Profit: +125,000,000 GP
```

## 🔧 Troubleshooting

### Build Issues
```bash
# Check Java version
java -version  # Should be 11+

# Clean build
./gradlew clean build --refresh-dependencies
```

### Script Issues
- **Not accepting trades**: Enable "Auto Accept Trades" in GUI
- **State stuck**: Script auto-recovers after 45 seconds
- **Dice not rolling**: Verify CRAPS_DICE_ID (15098) is correct

### Compilation Errors
See `COMPILATION_FIXES.md` and `DREAMBOT3_API_FIXES.md` for resolved issues.

## 📚 Documentation

- **[CRAPS_GAME_GUIDE.md](CRAPS_GAME_GUIDE.md)** - Complete Craps game documentation
- **[COMPILATION_FIXES.md](COMPILATION_FIXES.md)** - Compilation error fixes
- **[DREAMBOT3_API_FIXES.md](DREAMBOT3_API_FIXES.md)** - DreamBot 3 API compatibility
- **[README_ELITE.md](README_ELITE.md)** - Elite edition documentation
- **[WINDOWS_SETUP.md](WINDOWS_SETUP.md)** - Windows-specific setup guide

## 🔐 Security & Fair Play

### Provably Fair RNG (Craps)
- **Seed Generation**: Player name + timestamp + secure random hex
- **SHA-256 Hashing**: Seed hashed before roll
- **Verification**: Full seed revealed after roll
- **Transparency**: All rolls can be independently verified

### Trade Safety
- Validates trade amounts before acceptance
- Automatic decline of invalid trades
- Timeout protection for stuck states
- Full logging for audit trails

## 📈 Version History

### v7.0 (Current) - January 2026
- ✨ **NEW**: Craps game with provably fair RNG
- ✨ **NEW**: Double or Nothing feature
- ✨ **NEW**: SHA-256 seed verification
- 🔧 Enhanced GUI with more options
- 🔧 Improved state machine reliability
- 🔧 Better error handling and recovery
- 📚 Comprehensive documentation

### v6.0
- Full implementation of Dice, Wheel, Roulette
- Trade safety features
- Auto-advertising
- Session recovery

## 📄 License

This project is open source. See [LICENSE](LICENSE) for details.

## 📞 Support

For issues or feature requests:
1. Open an issue on [GitHub](https://github.com/No6love9/ikingsnipe)
2. Check documentation in repository
3. Review [DreamBot API docs](https://dreambot.org/javadocs/)

---

**Made with ❤️ for the OSRS community**

**Elite Titan Casino v7.0** - *The most advanced OSRS casino bot with provably fair Craps!* 🎲🎰
