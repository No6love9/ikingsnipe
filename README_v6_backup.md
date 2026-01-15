# Elite Titan Casino - Full Implementation

A professional-grade OSRS casino bot for DreamBot 3, featuring complete logic for Dice, Wheel, and Roulette games.

## Project Structure
```text
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── EliteTitanCasino.java   # Core script logic (Fully Implemented)
├── libs/
│   └── dreambot-api.jar        # DreamBot 3 API dependency
├── build.gradle                # Gradle build configuration
└── README.md                   # This file
```

## Features
- **Full Game Logic**: Complete interaction steps for Dice, Wheel, and Roulette.
- **Trade Safety**: Automatic verification of trade amounts and safe acceptance.
- **Configurable GUI**: Set your game, bet amount, and custom messages on startup.
- **Auto-Advertising**: Built-in chat automation to attract players.
- **Session Recovery**: Handles logins and state resets automatically.

## Setup Instructions

### 1. Prerequisites
- **Java JDK 11** or higher.
- **DreamBot Client** installed.

### 2. Local Compilation
To build the script locally, use the provided Gradle wrapper:
```bash
./gradlew clean build
```
The compiled JAR will be located in the `output/` directory as `EliteTitanCasino.jar`.

### 3. Running the Script
1. Copy `EliteTitanCasino.jar` to your DreamBot scripts folder (usually `C:\Users\YourName\DreamBot\Scripts\`).
2. Open the DreamBot client and log in to OSRS.
3. Start the script from the script manager.
4. Configure your settings in the popup GUI and click **Start Script**.

## Support
For issues or feature requests, please open an issue on the GitHub repository.
