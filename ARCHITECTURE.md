# iKingSnipe Bot - Complete Architecture Design

## Overview
A unified bot system integrating DreamBot in-game automation with Discord bot control, packaged as a single JAR file.

## Core Components

### 1. Main Application Controller
**Class**: `com.ikingsnipe.core.BotApplication`
- Manages lifecycle of all subsystems
- Coordinates between DreamBot script and Discord bot
- Handles configuration loading and persistence
- Provides unified logging system

### 2. DreamBot Script Layer
**Package**: `com.ikingsnipe.dreambot`

#### Main Script
- **EliteTitanCasino.java** - Main DreamBot script entry point
- Extends `AbstractScript` from DreamBot API
- Implements all casino game logic
- Handles trade automation
- Anti-ban and humanization

#### Managers
- **GameManager** - Game logic execution
- **TradeManager** - Trade window handling
- **BankingManager** - Banking operations
- **LocationManager** - Walking and positioning
- **HumanizationManager** - Anti-ban behaviors
- **MuleManager** - Automated muling
- **ProfitTracker** - Session statistics

#### Game Implementations
- **CrapsGame** - Chasing Craps 3x payout
- **DiceDuelGame** - Classic dice dueling
- **FlowerPokerGame** - Flower poker
- **BlackjackGame** - Blackjack
- **HotColdGame** - Hot/Cold game
- **FiftyFiveGame** - 55x2 game
- **DiceWarGame** - Dice war

### 3. Discord Bot Layer
**Package**: `com.ikingsnipe.discord`

#### Core Components
- **DiscordBotManager** - JDA-based Discord bot controller
- **CommandHandler** - Slash command processor
- **EventListener** - Discord event handling
- **EmbedBuilder** - Rich embed formatting

#### Commands
- `/start` - Start the in-game bot
- `/stop` - Stop the in-game bot
- `/status` - Get current bot status
- `/stats` - View profit/loss statistics
- `/config` - Update bot configuration
- `/balance` - Check player balances
- `/link` - Link Discord user to RS account
- `/jackpot` - View current jackpot
- `/mule` - Trigger mule transfer
- `/emergency` - Emergency stop all operations

### 4. Unified GUI
**Package**: `com.ikingsnipe.gui`

#### Main GUI Components
- **MainControlPanel** - Primary control interface
  - Start/Stop buttons for both bots
  - Real-time status indicators
  - Configuration tabs
  - Log viewer with filtering
  
- **ConfigurationPanel** - Settings management
  - Game selection and rules
  - Betting limits
  - Discord bot token
  - Database credentials
  - Location settings
  - Humanization parameters
  
- **StatisticsPanel** - Live statistics dashboard
  - Profit/loss graphs (JFreeChart)
  - Session duration
  - Games played counter
  - Win/loss ratio
  - Hourly profit rate
  
- **PlayerManagementPanel** - Player database viewer
  - Search and filter players
  - View player history
  - Blacklist management
  - VIP player settings
  
- **LogPanel** - Integrated log viewer
  - Color-coded log levels
  - Search and filter
  - Export logs
  - Auto-scroll toggle

### 5. Database Layer
**Package**: `com.ikingsnipe.database`

#### Components
- **DatabaseManager** - Connection pool management
- **PlayerRepository** - Player CRUD operations
- **GameHistoryRepository** - Game records
- **ConfigRepository** - Persistent configuration
- **StatisticsRepository** - Aggregated stats

#### Schema
```sql
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    discord_id VARCHAR(50),
    balance_gp BIGINT DEFAULT 0,
    total_wagered BIGINT DEFAULT 0,
    total_won BIGINT DEFAULT 0,
    total_lost BIGINT DEFAULT 0,
    games_played INT DEFAULT 0,
    is_vip BOOLEAN DEFAULT FALSE,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    bet_amount BIGINT NOT NULL,
    payout_amount BIGINT DEFAULT 0,
    result VARCHAR(20) NOT NULL,
    seed_hash VARCHAR(64),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id)
);

CREATE TABLE bot_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE bot_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_profit BIGINT DEFAULT 0,
    games_played INT DEFAULT 0,
    runtime_minutes INT DEFAULT 0
);
```

### 6. Shared State Manager
**Package**: `com.ikingsnipe.core.state`

#### Components
- **BotStateManager** - Thread-safe state coordination
- **EventBus** - Internal event system for component communication
- **ConfigManager** - Centralized configuration access

#### State Model
```java
public class BotState {
    private volatile boolean dreambotRunning;
    private volatile boolean discordBotRunning;
    private volatile CasinoState gameState;
    private volatile long sessionProfit;
    private volatile int gamesPlayed;
    private volatile String currentPlayer;
    private volatile Map<String, Object> runtimeConfig;
}
```

### 7. Communication Layer
**Package**: `com.ikingsnipe.core.communication`

#### Components
- **EventBus** - Publish-subscribe event system
- **MessageQueue** - Async message handling between components
- **WebhookManager** - Discord webhook notifications

#### Event Types
- `BotStartedEvent`
- `BotStoppedEvent`
- `GameCompletedEvent`
- `TradeAcceptedEvent`
- `ProfitUpdateEvent`
- `ErrorEvent`
- `ConfigChangedEvent`

## Data Flow

### Game Execution Flow
```
Player trades bot → TradeManager detects trade
→ GameManager selects game logic
→ Game executes (dice roll, etc.)
→ Result calculated
→ Database updated
→ Discord notification sent
→ GUI updated
→ Trade completed
```

### Discord Command Flow
```
User sends /start command
→ DiscordBotManager receives command
→ CommandHandler validates permissions
→ BotStateManager updates state
→ DreamBot script receives start signal
→ Confirmation sent to Discord
→ GUI updated
```

### GUI Control Flow
```
User clicks Start button
→ MainControlPanel triggers event
→ BotApplication starts DreamBot thread
→ State updated in BotStateManager
→ Discord bot notified
→ Status indicators updated
```

## Threading Model

### Thread Architecture
1. **Main GUI Thread** - Swing EDT for UI updates
2. **DreamBot Script Thread** - DreamBot's onLoop execution
3. **Discord Bot Thread** - JDA event handling
4. **Database Worker Pool** - Async database operations (5 threads)
5. **Event Bus Thread** - Event dispatch
6. **Webhook Thread** - Discord webhook sender

### Synchronization Strategy
- Use `ConcurrentHashMap` for shared state
- `AtomicLong` for profit tracking
- `ReentrantLock` for critical sections
- Event bus for loose coupling

## Build Configuration

### Gradle Setup
```gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    // DreamBot API
    compileOnly fileTree(dir: 'libs', include: ['dreambot-*.jar'])
    
    // Discord JDA
    implementation 'net.dv8tion:JDA:5.0.0-beta.20'
    
    // Database
    implementation 'com.zaxxer:HikariCP:4.0.3'
    implementation 'mysql:mysql-connector-java:8.0.33'
    
    // GUI Charts
    implementation 'org.jfree:jfreechart:1.5.4'
    
    // Utilities
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

shadowJar {
    archiveFileName = 'iKingSnipe-Complete.jar'
    
    // Relocate to avoid conflicts
    relocate 'com.google.gson', 'com.ikingsnipe.libs.gson'
    relocate 'com.mysql', 'com.ikingsnipe.libs.mysql'
    relocate 'com.zaxxer.hikari', 'com.ikingsnipe.libs.hikari'
    relocate 'net.dv8tion.jda', 'com.ikingsnipe.libs.jda'
    relocate 'org.jfree', 'com.ikingsnipe.libs.jfree'
    
    // Exclude signatures to avoid security exceptions
    exclude 'META-INF/*.SF'
    exclude 'META-INF/*.DSA'
    exclude 'META-INF/*.RSA'
    
    manifest {
        attributes(
            'Main-Class': 'com.ikingsnipe.core.BotApplication',
            'Implementation-Title': 'iKingSnipe Complete Bot',
            'Implementation-Version': '1.0.0',
            'Multi-Release': 'true'
        )
    }
}
```

## Deployment Structure

### JAR Contents
```
iKingSnipe-Complete.jar
├── com/ikingsnipe/
│   ├── core/
│   │   ├── BotApplication.class (Main entry point)
│   │   ├── state/
│   │   └── communication/
│   ├── dreambot/
│   │   ├── EliteTitanCasino.class (DreamBot script)
│   │   ├── managers/
│   │   ├── games/
│   │   └── listeners/
│   ├── discord/
│   │   ├── DiscordBotManager.class
│   │   ├── CommandHandler.class
│   │   └── EventListener.class
│   ├── gui/
│   │   ├── MainControlPanel.class
│   │   ├── ConfigurationPanel.class
│   │   └── StatisticsPanel.class
│   ├── database/
│   │   ├── DatabaseManager.class
│   │   └── repositories/
│   └── libs/ (relocated dependencies)
├── resources/
│   ├── config.json (default configuration)
│   ├── schema.sql (database schema)
│   └── icons/ (GUI icons)
└── META-INF/
    └── MANIFEST.MF
```

### Usage Modes

#### Mode 1: Standalone GUI Application
```bash
java -jar iKingSnipe-Complete.jar
```
Launches GUI with both DreamBot and Discord bot control.

#### Mode 2: DreamBot Script Only
Place JAR in DreamBot scripts folder, select from script selector.

#### Mode 3: Headless Discord Bot
```bash
java -jar iKingSnipe-Complete.jar --headless --discord-only
```

## Configuration Management

### Configuration File (config.json)
```json
{
  "dreambot": {
    "minBet": 1000000,
    "maxBet": 2147483647,
    "defaultGame": "craps",
    "location": "GRAND_EXCHANGE",
    "autoBank": true,
    "autoMule": false,
    "muleName": "",
    "humanization": {
      "enableBreaks": true,
      "breakFrequency": 60,
      "breakDuration": 5
    }
  },
  "discord": {
    "token": "YOUR_BOT_TOKEN",
    "enabled": true,
    "commandPrefix": "/",
    "adminRoleId": "ADMIN_ROLE_ID",
    "notificationChannelId": "CHANNEL_ID",
    "webhookUrl": "WEBHOOK_URL"
  },
  "database": {
    "host": "localhost",
    "port": 3306,
    "database": "ikingsnipe",
    "username": "root",
    "password": "",
    "poolSize": 5
  },
  "gui": {
    "theme": "dark",
    "autoScroll": true,
    "showNotifications": true
  }
}
```

## Security Considerations

1. **Configuration Encryption** - Sensitive data (tokens, passwords) encrypted at rest
2. **Permission System** - Discord role-based access control
3. **Rate Limiting** - Prevent command spam
4. **Input Validation** - Sanitize all user inputs
5. **SQL Injection Prevention** - Prepared statements only
6. **Secure Logging** - Redact sensitive information from logs

## Error Handling Strategy

### Levels
1. **Recoverable Errors** - Automatic retry with exponential backoff
2. **State Errors** - Reset to safe state, notify user
3. **Critical Errors** - Emergency stop, save state, notify admin
4. **Network Errors** - Queue operations, retry when connection restored

### Logging
- **TRACE** - Detailed execution flow
- **DEBUG** - Development information
- **INFO** - Normal operations
- **WARN** - Recoverable issues
- **ERROR** - Serious problems
- **FATAL** - System shutdown required

## Performance Optimization

1. **Connection Pooling** - HikariCP for database
2. **Lazy Loading** - Load GUI components on demand
3. **Batch Operations** - Bulk database inserts
4. **Caching** - Cache frequently accessed config
5. **Async Operations** - Non-blocking I/O where possible
6. **Memory Management** - Proper resource cleanup

## Testing Strategy

1. **Unit Tests** - Core logic components
2. **Integration Tests** - Database operations
3. **Mock Tests** - DreamBot API interactions
4. **GUI Tests** - Swing component validation
5. **Load Tests** - Concurrent trade handling

## Future Enhancements

1. **Web Dashboard** - Browser-based control panel
2. **Mobile App** - React Native companion app
3. **Machine Learning** - Predictive player behavior
4. **Multi-Account Support** - Manage multiple bots
5. **Cloud Sync** - Cross-device configuration sync
