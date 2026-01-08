# 🏆 ELITE TITAN CASINO v15.0 - ULTIMATE EDITION

**The most comprehensive, robust, and feature-complete casino system for DreamBot**

---

## 🚀 Quick Start (2 Steps!)

### Step 1: Build
```cmd
BUILD_ELITE.bat
```

### Step 2: Install
```cmd
INSTALL_ELITE.bat
```

**Done!** Open DreamBot → Scripts → "ELITE TITAN CASINO v15.0" → Start 🎉

---

## ✨ Features

### 🎰 13 Complete Casino Games
1. **Dice** - Roll 55+ to win 2x
2. **Flower Poker** - Beat the house hand
3. **Blackjack** - Get closer to 21
4. **55x2** - Classic doubler
5. **Roulette** - Red/Black betting
6. **Slots** - Match symbols for jackpot
7. **High-Low** - Over 50 wins
8. **Coin Flip** - Heads or tails
9. **Lucky 7** - Roll exactly 7 for 7x payout
10. **Hot Dice** - Tiered payouts (60+, 75+, 90+)
11. **Craps** - Classic dice game
12. **Poker Dice** - Five dice poker hands
13. **Custom** - Template for your own games

### 🛡️ Robust System
- **Auto-Setup** - First-run wizard creates all directories
- **Error Handling** - Try-catch on every operation with fallbacks
- **Auto-Backup** - Database backed up every 10 minutes
- **Error Recovery** - Automatic recovery from failures
- **Logging** - Detailed logs for debugging

### 💎 Professional GUI
- **Real-Time Stats** - Games played, profit, players
- **Player Table** - View all balances sorted by amount
- **Activity Log** - Live feed of all events
- **Configuration** - Clan chat, Discord webhook setup
- **Management** - Save, backup, refresh controls

### 🎲 Provably Fair
- **HMAC-SHA512** - Cryptographic random number generation
- **Verifiable** - Cannot be manipulated or predicted
- **Transparent** - Fair for all players

### 🔧 Advanced Features
- **Discord Webhooks** - Real-time game notifications
- **Clan Chat Integration** - Auto-join and announce
- **Message Queue** - Anti-mute system
- **Trade System** - Automatic deposits/withdrawals
- **Blacklist** - Block problem players
- **Statistics** - Comprehensive tracking

### 🔌 Module System
- **Plugin Architecture** - Easy to add new games
- **Game Template** - Custom game included
- **Hot-Reload** - Add games without restart
- **API** - Simple interface for new games

---

## 📋 Requirements

- **Java 8+** - https://www.java.com/
- **DreamBot Client** - https://dreambot.org/
- **Windows** - Batch scripts for Windows (Mac/Linux use Gradle)

---

## 📖 Detailed Instructions

### 1. Download Repository

```cmd
git clone https://github.com/No6love9/ikingsnipe.git
cd ikingsnipe
```

Or download ZIP from GitHub and extract.

### 2. Get DreamBot JAR

**Option A: Automatic (Recommended)**
- Run `BUILD_ELITE.bat`
- It will auto-detect and copy from `%USERPROFILE%\.dreambot\cache\`

**Option B: Manual**
1. Run DreamBot once
2. Go to: `%USERPROFILE%\.dreambot\cache\`
3. Copy `client-X.X.X.jar` to `libs\` folder

### 3. Build

Double-click: **`BUILD_ELITE.bat`**

You'll see:
```
═══════════════════════════════════════════════════════════════════════════
  ELITE TITAN CASINO v15.0 - ULTIMATE BUILD SYSTEM
═══════════════════════════════════════════════════════════════════════════

[1/8] Checking Java installation... ✓
[2/8] Checking for DreamBot client JAR... ✓
[3/8] Cleaning previous build... ✓
[4/8] Locating source files... ✓
[5/8] Compiling Java source code... ✓
[6/8] Creating JAR manifest... ✓
[7/8] Packaging JAR file... ✓
[8/8] Verifying output... ✓

═══════════════════════════════════════════════════════════════════════════
  BUILD SUCCESSFUL!
═══════════════════════════════════════════════════════════════════════════
```

### 4. Install

Double-click: **`INSTALL_ELITE.bat`**

This copies the JAR to `%USERPROFILE%\.dreambot\scripts\`

### 5. Run in DreamBot

1. Open **DreamBot**
2. Click **"Scripts"**
3. Find **"ELITE TITAN CASINO v15.0"**
4. Click **"Start"**
5. **Professional GUI** will appear
6. Configure clan chat and Discord webhook (optional)
7. Your casino is now **ONLINE**! 🎰

---

## 🎮 How to Use

### For Casino Operators

#### First Run
- Script auto-creates all directories in `%USERPROFILE%\.elitetitan\`
- Database, config, and backups are stored there
- Professional GUI launches automatically

#### Configuration
1. **Clan Chat** - Enter name and click "Set"
2. **Discord Webhook** - Enter URL and click "Set"
3. **View Players** - Click "Refresh" to see all balances
4. **Save Data** - Click to manually save database
5. **Backup** - Click to create backup

#### Monitoring
- **Paint Overlay** - Shows runtime, games, profit, players on-screen
- **Activity Log** - Shows all events in GUI
- **Player Table** - Shows all player balances

### For Players

#### Commands
```
!dice <amount>    - Play dice (55+ wins 2x)
!fp <amount>      - Flower poker
!bj <amount>      - Blackjack
!55x2 <amount>    - 55x2 game
!roulette <amount> - Roulette
!slots <amount>   - Slots
!highlow <amount> - High-Low
!flip <amount>    - Coin flip
!lucky7 <amount>  - Lucky 7
!hotdice <amount> - Hot Dice
!craps <amount>   - Craps
!poker <amount>   - Poker Dice
!custom <amount>  - Custom game

!balance          - Check balance
!withdraw         - Withdraw winnings
!help             - Show commands
```

#### Deposit
1. Trade the casino bot
2. Put coins in trade
3. Accept trade
4. Balance is credited

#### Withdraw
1. Type `!withdraw`
2. Trade the casino bot
3. Receive your coins

#### Play Games
1. Type game command with bet amount
2. Example: `!dice 100k`
3. Result is announced in chat
4. Winnings are credited to balance

---

## 🎯 Game Rules

### Dice
- Roll 1-100
- 55+ wins 2x bet
- House edge: 10%

### Flower Poker
- You and house each get 0-9
- Higher number wins 2x
- Tie returns bet

### Blackjack
- You and house each get 2 cards (1-11)
- Closer to 21 wins 2x
- Over 21 loses

### 55x2
- Roll 1-100
- 55+ wins 2x bet

### Roulette
- Number 0-36
- Odd numbers (except 0) win 2x
- Even numbers lose

### Slots
- 3 symbols
- All 3 match: 10x
- 2 match: 2x
- No match: lose

### High-Low
- Roll 1-100
- Over 50 wins 2x

### Coin Flip
- Heads or tails
- 50/50 chance
- Win: 2x bet

### Lucky 7
- Roll 2 dice (1-6 each)
- Sum of exactly 7 wins 7x
- Any other sum loses

### Hot Dice
- Roll 1-100
- 90+: 10x
- 75-89: 3x
- 60-74: 2x
- Under 60: lose

### Craps
- Roll 2 dice
- 7 or 11: win 2x
- 2, 3, or 12: lose
- Other: push (return bet)

### Poker Dice
- Roll 5 dice
- Five of a kind: 100x
- Four of a kind: 20x
- Full house: 10x
- Three of a kind: 5x
- Two pair: 3x
- One pair: 2x
- Nothing: lose

### Custom
- Template for your own game
- Modify in source code

---

## 🔧 Customization

### Adding New Games

1. **Create Game Class**
```java
class MyNewGame implements CasinoGame {
    private final CasinoEngine engine;
    
    public MyNewGame(CasinoEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public String[] getCommands() {
        return new String[]{"!mygame"};
    }
    
    @Override
    public void play(String player, String command) {
        // Your game logic here
    }
}
```

2. **Register in GameRegistry**
```java
registerGame(new MyNewGame(engine));
```

3. **Rebuild**
```cmd
BUILD_ELITE.bat
INSTALL_ELITE.bat
```

### Modifying Payouts

Edit the game class and change payout calculations:
```java
long payout = roll >= 55 ? bet * 3 : 0; // Changed from 2x to 3x
```

### Changing Win Conditions

```java
if (roll >= 60) { // Changed from 55
    payout = bet * 2;
}
```

---

## 📊 File Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── EliteTitanCasino.java (Complete system - 3000+ lines)
├── libs/
│   └── client-X.X.X.jar (DreamBot JAR - auto-detected)
├── output/
│   └── EliteTitanCasino-15.0.jar (Compiled script)
├── BUILD_ELITE.bat (Elite build system)
├── INSTALL_ELITE.bat (Automatic installer)
├── README_ELITE.md (This file)
└── .gitignore

User Data (Auto-created):
%USERPROFILE%\.elitetitan/
├── config/
│   ├── casino.json (Configuration)
│   └── setup.flag (Setup complete marker)
├── data/
│   └── players.db (Player database)
└── backups/
    └── players_YYYYMMDD_HHMMSS.db (Auto-backups)
```

---

## 🆘 Troubleshooting

### "Java not found"
**Fix**: Install Java from https://www.java.com/ and restart

### "DreamBot JAR not found"
**Fix**:
1. Run DreamBot once
2. Go to `%USERPROFILE%\.dreambot\cache\`
3. Copy `client-X.X.X.jar` to `libs\` folder
4. Run BUILD_ELITE.bat again

### "Compilation failed"
**Fix**:
1. Make sure DreamBot JAR is in `libs\`
2. Check Java version: `java -version` (need 8+)
3. Delete `build` and `output` folders
4. Run BUILD_ELITE.bat again

### "Script doesn't appear in DreamBot"
**Fix**:
1. Make sure you ran INSTALL_ELITE.bat
2. Check `%USERPROFILE%\.dreambot\scripts\` for the JAR
3. Restart DreamBot
4. Click "Refresh" in scripts menu

### "GUI doesn't appear"
**Fix**:
1. Check console for errors
2. Make sure Java GUI libraries are installed
3. Try running DreamBot as administrator

### "Players can't deposit/withdraw"
**Fix**:
1. Make sure bot has coins in inventory
2. Check trade settings in DreamBot
3. Verify script is running (green play button)

### "Games not working"
**Fix**:
1. Check activity log in GUI for errors
2. Verify players have balance
3. Make sure commands are typed correctly
4. Check if player is blacklisted

---

## 💡 Tips & Best Practices

### For Best Results
- Set a clan chat to keep games organized
- Configure Discord webhook for notifications
- Check player table regularly
- Use backup feature before major changes
- Monitor activity log for issues

### Security
- Player data saved locally (encrypted)
- Blacklist feature for problem players
- Provably fair RNG (cannot be cheated)
- All trades validated

### Performance
- Auto-backup every 10 minutes
- Message queue prevents muting
- Error handling on all operations
- Automatic recovery from failures

### Customization
- Edit source code to change rules
- Add new games easily
- Modify payouts
- Change win conditions

---

## 📈 Statistics

The system tracks:
- Total games played
- Total profit/loss
- Player count
- Individual player balances
- Game-specific statistics

View in:
- Paint overlay (on-screen)
- GUI statistics panel
- Activity log

---

## 🎉 Summary

### What You Get
- ✅ 13 complete casino games
- ✅ Professional GUI
- ✅ Auto-setup system
- ✅ Robust error handling
- ✅ Provably fair RNG
- ✅ Discord integration
- ✅ Auto-backup system
- ✅ Module system for new games
- ✅ Comprehensive documentation

### What You Need
1. Java 8+
2. DreamBot client
3. 5 minutes to build and install

### What You Do
1. Run BUILD_ELITE.bat
2. Run INSTALL_ELITE.bat
3. Start in DreamBot
4. Configure in GUI
5. Dominate! 🎰

---

## 📝 Version History

### v15.0 ELITE (Current)
- Complete rewrite for DreamBot 2025/2026 API
- 13 casino games
- Professional GUI
- Auto-setup system
- Robust error handling
- Module system
- Discord integration
- Auto-backup

### v14.0
- Modern API update
- 4 games
- Basic GUI

### v13.0 and earlier
- Legacy versions

---

## 📧 Support

- **GitHub**: https://github.com/No6love9/ikingsnipe
- **Issues**: https://github.com/No6love9/ikingsnipe/issues

---

## ⚠️ Disclaimer

**For educational purposes only.**

- Use at your own risk
- May violate game terms of service
- No warranty or guarantee provided
- Author not responsible for bans or issues

---

## 🏆 Credits

**Made with ❤️ by iKingSnipe**

**ELITE TITAN CASINO v15.0 - The Ultimate Casino System** ✨

---

**Your elite casino empire starts now!** 🎰🚀
