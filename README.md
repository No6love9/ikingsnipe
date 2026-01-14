# 🎰 Elite Titan Casino ULTIMATE v4.2

**The most complete and robust OSRS casino bot for DreamBot 3.**

This script is a professional-grade solution featuring automatic login handling, full game logic for Dice, Wheel, and Roulette, and a stable, non-crashing configuration GUI.

---

## 🚀 Features

- **✅ Automatic Login & Session Management**: Handles the login screen and connection loss automatically.
- **✅ Full Game Logic**: Complete interaction logic for **Dice**, **Wheel**, and **Roulette** games.
- **✅ Robust Trade Safety**: Scans trade windows, verifies coin amounts, and only accepts safe trades.
- **✅ Configurable Chat Automation**: Automated advertising and game result announcements (Win/Loss).
- **✅ Professional GUI**: Easy-to-use configuration panel for all bot settings.
- **✅ Real-Time Statistics**: On-screen overlay tracking wins, losses, and total profit.

---

## 🛠️ Setup & Installation

### Prerequisites
- **Java JDK 11** or higher.
- **DreamBot 3** client.
- **Gradle** (included in the project via wrapper).

### Local Compilation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/No6love9/ikingsnipe.git
   cd ikingsnipe
   ```

2. **Build the JAR**:
   Run the following command in your terminal:
   ```bash
   ./gradlew clean shadowJar
   ```
   *Note: On Windows, use `gradlew.bat clean shadowJar`.*

3. **Locate the JAR**:
   The compiled "Fat JAR" will be located at:
   `build/libs/EliteTitanCasino-1.1.0.jar`

### Running the Bot
1. Copy the compiled JAR to your DreamBot scripts folder:
   `%USERPROFILE%\DreamBot\Scripts\` (Windows) or `~/DreamBot/Scripts/` (Mac/Linux).
2. Open the DreamBot client and refresh your scripts list.
3. Start **Elite Titan Casino ULTIMATE**.
4. Configure your settings in the popup GUI and click **Start Bot**.

---

## ⚙️ Configuration Options

- **Game Selection**: Choose between Dice, Wheel, or Roulette.
- **Bet Amount**: Set the amount of coins to bet per game.
- **Min Trade**: The minimum amount of coins required to accept a trade.
- **Auto Accept Trade**: Toggle whether the bot should automatically handle trades.
- **Custom Messages**: Edit your Ad, Win, and Loss messages directly in the GUI.

---

## 📁 Project Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   └── EliteTitanCasino.java   # Core script logic
├── libs/
│   └── dreambot-api.jar        # DreamBot 3 API dependency
├── build.gradle                # Gradle build configuration
└── README.md                   # This file
```

---

## ⚠️ Safety & Disclaimer

**For educational purposes only.**

- Use at your own risk.
- Botting in OSRS carries a risk of account suspension.
- The author is not responsible for any bans or losses incurred.

---

**Developed by ikingsnipe** 🎰
