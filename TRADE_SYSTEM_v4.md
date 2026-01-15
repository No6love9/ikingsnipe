# snipes♧scripts Enterprise v4.0 - Trade Handling System

## Overview

This update completely overhauls the trade handling system to fix issues with trade request detection, add comprehensive anti-scam protection, and provide full configuration options for all trade-related features.

## New Files Created

| File | Description |
|------|-------------|
| `TradeManager.java` | Core trade handling with request detection, verification, and lifecycle management |
| `TradeConfig.java` | 30+ configurable trade settings with preset support |
| `TradeRequestListener.java` | Chat-based trade request detection listener |
| `TradeStatistics.java` | Trade performance tracking and player trust system |

## Key Fixes

### 1. Trade Request Detection (FIXED)
**Problem:** Bot wasn't accepting trade offers/requests during idle or advertising states.

**Solution:** 
- Added `TradeManager.detectTradeRequest()` with multiple detection methods:
  - Checks if trade window is already open
  - Detects players interacting with the bot
  - Monitors trade request widgets
  - Queues requests for later processing
- `TradeRequestListener` captures trade requests from game messages

### 2. Trade Awareness During Idle/Advertising (FIXED)
**Problem:** Bot was unaware of incoming trades while advertising.

**Solution:**
- Enhanced `handleIdle()` method now calls `tradeManager.detectTradeRequest()` every loop
- Trade detection runs continuously regardless of advertising state
- Pending trade requests are queued and processed in order

### 3. Anti-Scam Verification (IMPLEMENTED)
**Problem:** Basic verification was slow and incomplete.

**Solution:**
- **Value Stability Check:** Trade value must remain stable for configurable duration
- **Multi-Check Verification:** Higher value trades require more verification passes
- **Screen 2 Double-Check:** Verifies values match between trade screens
- **Final Check:** Last-second verification before accepting
- **Scam Logging:** All scam attempts are logged and tracked

### 4. Player-Friendly Speed (IMPLEMENTED)
**Problem:** Verification was too slow for legitimate players.

**Solution:**
- **Trade Presets:** Choose from Fast & Friendly, Balanced, Maximum Security, or High Roller
- **Trusted Player System:** Returning players with successful history get reduced verification
- **Auto-Accept Small Bets:** Optional instant acceptance for bets under threshold
- **Configurable Delays:** Fine-tune all timing parameters

## Configuration Options

### Trade Presets

| Preset | Description |
|--------|-------------|
| **Fast & Friendly** | Quick trades, minimal verification, great player experience |
| **Balanced** | Good security with reasonable speed (default) |
| **Maximum Security** | Thorough verification, slower but safest |
| **High Roller** | Optimized for large value trades with extended timeouts |

### Anti-Scam Settings

```java
enableAntiScam = true;              // Master toggle for anti-scam
valueStabilityTime = 600;           // ms value must be stable
minVerifyDelay = 300;               // Minimum delay between checks
maxVerifyDelay = 600;               // Maximum delay between checks
enableScreen2Verification = true;   // Double-check on screen 2
```

### Value Thresholds

```java
mediumValueThreshold = 10_000_000L;  // 10M - requires more verification
highValueThreshold = 100_000_000L;   // 100M - requires most verification
lowValueVerifyCount = 1;             // Checks for low value
mediumValueVerifyCount = 2;          // Checks for medium value
highValueVerifyCount = 3;            // Checks for high value
```

### Timing Settings

```java
tradeTimeout = 60000;           // Overall trade timeout (60s)
screen2Timeout = 30000;         // Screen 2 timeout (30s)
tradeAcceptTimeout = 5000;      // Time to accept trade request (5s)
screen2WaitTime = 5000;         // Wait for screen 2 to open (5s)
tradeCompleteWaitTime = 5000;   // Wait for trade to complete (5s)
```

### Player Experience

```java
sendWelcomeMessage = true;           // Send greeting when trade opens
sendGameCommands = true;             // Show available game commands
sendConfirmationMessages = true;     // Confirm bet amounts
enableFastAcceptReturning = true;    // Quick accept for returning players
reducedVerifyForTrusted = true;      // Less verification for trusted players
trustedPlayerTradeCount = 3;         // Trades needed to become trusted
autoAcceptSmallBets = false;         // Instant accept for small bets
smallBetThreshold = 1_000_000L;      // Threshold for small bets
```

### Custom Messages

```java
customWelcomeMessage = "Welcome {player}! Safe trade active. Hash: {hash}";
// Placeholders: {player}, {hash}, {min}, {max}
```

## GUI Configuration

The new **Trade** tab in the GUI provides access to all settings:

1. **Trade Preset** - Quick preset selection with descriptions
2. **Anti-Scam Protection** - Enable/disable and configure verification
3. **Value Thresholds** - Set medium and high value limits
4. **Verification Counts** - Configure checks per value tier
5. **Timing** - Adjust all timeout values
6. **Trade Messages** - Toggle welcome, commands, and confirmations
7. **Custom Welcome** - Edit the welcome message template
8. **Player Experience** - Configure trusted player and auto-accept features
9. **Advanced** - Trade queue, logging, and distance settings

## Trade Statistics

The system now tracks comprehensive trade statistics:

- Total trades attempted/completed/declined/failed
- Total value traded and payouts given
- Scam attempts detected and blocked
- Average trade time, fastest, and slowest
- Per-player statistics including trust level
- Recent trade history

### Player Trust Levels

| Level | Criteria |
|-------|----------|
| **New** | No trade history |
| **Known** | 1-2 successful trades |
| **Trusted** | 3+ successful trades, no scam attempts |
| **Suspicious** | Any scam attempt detected |

## Trade Flow

```
IDLE
  │
  ├─► detectTradeRequest()
  │     ├─► Check if trade window open
  │     ├─► Find player interacting with us
  │     ├─► Check trade request widgets
  │     └─► Process queued requests
  │
  ▼
TRADING_WINDOW_1
  │
  ├─► Send welcome message
  ├─► Validate bet limits
  ├─► Anti-scam verification
  │     ├─► Check value stability
  │     ├─► Multiple verification passes
  │     └─► Final check before accept
  ├─► Accept trade
  │
  ▼
TRADING_WINDOW_2
  │
  ├─► Screen 2 verification
  │     └─► Verify values match screen 1
  ├─► Accept trade
  │
  ▼
PROCESSING_GAME
  │
  ├─► Play selected game
  ├─► Announce result
  │
  ▼
PAYOUT_PENDING (if win)
  │
  ├─► Find player
  ├─► Initiate trade
  ├─► Add payout items
  ├─► Complete trade
  │
  ▼
IDLE (reset)
```

## Anti-Scam Detection

The system detects and blocks:

1. **Value Changes** - Player modifies trade amount after initial offer
2. **Screen Mismatches** - Values differ between screen 1 and 2
3. **Final Check Fails** - Last-second value modification
4. **Timeout Exploitation** - Excessive delays to confuse the bot

All attempts are logged with player name and type for review.

## Building

```bash
./gradlew shadowJar
```

Output: `build/libs/snipes_scripts_enterprise.jar`

## Version History

- **v4.0** - Comprehensive Trade Handling System
  - TradeManager with full lifecycle management
  - TradeConfig with 30+ settings and presets
  - TradeRequestListener for chat-based detection
  - TradeStatistics with player trust system
  - New Trade tab in GUI
  - Anti-scam verification with value stability
  - Screen 2 double-check verification
  - Player-friendly speed optimizations

---

*snipes♧scripts Enterprise - Professional Casino Hosting*
