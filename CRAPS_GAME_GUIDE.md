# 🎲 Elite Titan Casino - Craps Game Guide

## Overview

The **Craps game** has been fully integrated into Elite Titan Casino v7.0 with professional-grade implementation, provably fair RNG, and seamless DreamBot API 4 compatibility.

## Game Rules

### Win Conditions
- Roll **7**: WIN (3x payout)
- Roll **9**: WIN (3x payout)  
- Roll **12**: WIN (3x payout)

### Loss Conditions
- Roll **2, 3, 4, 5, 6, 8, 10, 11**: LOSS

### Payout Structure
- **Win**: 3x bet amount
- **Loss**: Lose bet amount
- **House Edge**: ~58.33% (5 losing outcomes vs 3 winning outcomes)

## Features

### ✅ Provably Fair RNG
- **Seed Generation**: Uses player name + timestamp + secure random hex
- **SHA-256 Hash**: Seed is hashed for verification
- **Deterministic Rolls**: Same seed always produces same dice results
- **Transparency**: Seed hash announced before roll, full seed revealed after

### ✅ Professional Implementation
- **State Machine**: Proper CRAPS_ROLLING and CRAPS_RESULT states
- **Error Handling**: Timeout protection and state recovery
- **Logging**: Full verification info logged for auditing
- **Trade Safety**: Validates bet amounts before game starts

### ✅ Double or Nothing (Optional)
- After winning, players can trade their winnings back to roll again
- Configurable via GUI checkbox
- Increases engagement and excitement

### ✅ DreamBot API 4 Compatible
- Uses latest DreamBot methods
- Proper async handling with state machine
- GUI configuration with Swing
- Paint overlay for real-time stats

## How to Use

### 1. Configuration
When starting the script, configure:
- **Select Game**: Choose "Craps" from dropdown
- **Bet Amount**: Minimum bet (e.g., 1,000,000 GP)
- **Min Trade**: Minimum trade amount to accept
- **Ad Message**: Customize your advertising message
- **Win/Loss Messages**: Customize game result messages
- **Auto Accept Trades**: Enable/disable automatic trade acceptance
- **Enable Double or Nothing**: Enable/disable double or nothing feature

### 2. Game Flow

#### Step 1: Advertising
```
Elite Casino | Fast Payouts | Dice, Wheel, Roulette, Craps!
```

#### Step 2: Trade Acceptance
- Player trades bet amount (e.g., 10M GP)
- Script validates amount >= minimum bet
- Trade accepted automatically (if enabled)

#### Step 3: Dice Roll
```
🎲 Rolling craps for PlayerName... Bet: 10,000,000 GP
🎲 Dice: 4 + 3 = 7
```

#### Step 4: Result Announcement
**If WIN (7, 9, or 12):**
```
🎉 PlayerName WINS! Payout: 30,000,000 GP (3x)
💰 Double or Nothing? Trade me 30,000,000 GP to roll again!
🔐 Verify: a3f5d8e2c1b4f6a9...
```

**If LOSS (any other total):**
```
❌ PlayerName loses! Total: 8 (Need 7, 9, or 12)
Better luck next time! House wins.
```

### 3. Verification
Full verification info logged:
```
Verification: Seed Hash: a3f5d8e2c1b4f6a9e7d3c5b1f8a2d4e6... | Seed: PlayerName-1736877600000-a1b2c3d4e5f6...
```

## Code Structure

### CrapsRound Class
```java
private static class CrapsRound {
    private final String seed;           // Provably fair seed
    private final String seedHash;       // SHA-256 hash
    private final int dice1;             // First die (1-6)
    private final int dice2;             // Second die (1-6)
    private final int total;             // Sum of dice
    private final boolean isWin;         // Win condition check
    private final long timestamp;        // Roll timestamp
}
```

### State Machine
```
IDLE → ADVERTISING → TRADING → GAMING → CRAPS_ROLLING → CRAPS_RESULT → IDLE
```

### Key Methods
- `handleCrapsGame()`: Initializes craps round with RNG
- `handleCrapsRolling()`: Announces dice results
- `handleCrapsResult()`: Processes win/loss and payouts

## Technical Details

### RNG Algorithm
```java
1. Generate seed: playerName + timestamp + secureRandomHex
2. Hash seed with SHA-256
3. Create Random object with seed.hashCode()
4. Roll dice: rng.nextInt(6) + 1 (twice)
5. Calculate total and check win conditions
```

### Win Probability
- **Winning outcomes**: 7 (6 ways), 9 (4 ways), 12 (1 way) = **11 combinations**
- **Total outcomes**: 36 possible dice combinations
- **Win probability**: 11/36 = **30.56%**
- **House edge**: 100% - (30.56% × 3) = **8.33%** (fair for 3x payout)

### Security Features
- **Seed verification**: Players can verify rolls were fair
- **Trade validation**: Prevents invalid bet amounts
- **State timeout**: Automatic recovery from stuck states
- **Error logging**: Full audit trail

## Paint Overlay

Real-time stats displayed:
```
Elite Titan Casino v7.0
State: CRAPS_RESULT
Game: Craps
Wins: 15 | Losses: 42
Current Player: PlayerName
Current Bet: 10,000,000 GP
Profit: +125,000,000 GP
```

## Compatibility

### DreamBot API Version
- **Compatible with**: DreamBot 3.x and 4.x
- **Tested on**: DreamBot API 4.0+
- **Java Version**: JDK 11+

### Dependencies
- `org.dreambot.api.*` - DreamBot API
- `javax.swing.*` - GUI components
- `java.security.*` - Cryptographic functions
- `java.util.Random` - RNG implementation

## Installation

### Method 1: Use Pre-compiled JAR
```bash
1. Navigate to output/ directory
2. Copy EliteTitanCasino.jar to DreamBot scripts folder
3. Start DreamBot and load the script
```

### Method 2: Build from Source
```bash
cd ikingsnipe
./gradlew clean build
# JAR will be in output/EliteTitanCasino.jar
```

### Windows Users
```batch
BUILD.bat
# or
BUILD_ELITE.bat
```

## Troubleshooting

### Issue: Script not accepting trades
**Solution**: Check "Auto Accept Trades" is enabled in GUI

### Issue: Dice not rolling
**Solution**: Ensure CRAPS_DICE_ID (15098) is correct for your OSRS version

### Issue: State stuck in GAMING
**Solution**: State timeout will auto-recover after 45 seconds

### Issue: Build fails
**Solution**: Ensure Java JDK 11+ is installed and JAVA_HOME is set

## Advanced Configuration

### Custom Win Conditions
Edit `CrapsRound` constructor to modify win logic:
```java
this.isWin = (total == 7 || total == 9 || total == 12);
// Change to: this.isWin = (total == 7 || total == 11);
```

### Custom Payout Multiplier
Edit `handleCrapsResult()` method:
```java
int payout = currentBetAmount * 3;  // 3x payout
// Change to: int payout = currentBetAmount * 2;  // 2x payout
```

### Custom Dice Range
Edit `CrapsRound` constructor:
```java
this.dice1 = rng.nextInt(6) + 1;  // 1-6
// Change to: this.dice1 = rng.nextInt(10) + 1;  // 1-10
```

## Credits

- **Original Concept**: ChasingCraps Discord bot
- **Conversion**: Adapted for DreamBot OSRS automation
- **Implementation**: Elite Titan Casino v7.0
- **Author**: ikingsnipe
- **Version**: 7.0
- **Date**: January 2026

## License

This project is licensed under the same terms as the ikingsnipe repository.

## Support

For issues, questions, or feature requests:
1. Open an issue on GitHub: https://github.com/No6love9/ikingsnipe
2. Check existing documentation in repository
3. Review DreamBot API documentation: https://dreambot.org/javadocs/

---

**Happy Gaming! 🎲🎰**
