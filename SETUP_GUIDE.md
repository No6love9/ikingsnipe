# iKingSnipe GoatGang Edition v14.0 - Comprehensive Setup Guide

This guide provides step-by-step instructions for setting up the iKingSnipe Casino Framework, the standalone Discord Bot, and the required MySQL database.

---

## 1. Initial Setup and File Structure

The final package contains the following structure:

```
ikingsnipe/
├── build/
│   └── libs/
│       └── ikingsnipe-14.0.0-GOATGANG.jar  <-- The standalone DreamBot script
├── discord_bot/
│   └── casino_bot.py                     <-- The standalone Python Discord Bot
├── setup.sh                              <-- Automated server setup script
├── SETUP_GUIDE.md                        <-- This document
├── ... (Source code and documentation)
```

### **Prerequisites**
1. **Java Development Kit (JDK)**: Required for running the DreamBot client.
2. **Python 3.x**: Required for running the Discord Bot.
3. **DreamBot Client**: Required for running the casino script.
4. **Discord Bot Token**: You must create a Discord Bot and obtain its token.

---

## 2. Automated Server Setup (MySQL)

The `setup.sh` script will automatically install MySQL, set the root password, and create the necessary database and tables for the casino framework.

### **Execution**
1. Open a terminal on your server (Linux/macOS).
2. Navigate to the `ikingsnipe` directory.
3. Run the setup script:
   ```bash
   ./setup.sh
   ```

### **Important Notes**
- **Default Password**: The script sets the MySQL root password to `goatgang_password`. **Change this immediately for production use.**
- **Database Name**: `goatgang`
- **Database User**: `root`
- **Tables Created**: `players` and `game_history`

---

## 3. Discord Bot Setup

The Discord Bot provides real-time statistics and administrative commands for your casino.

### **Step 3.1: Configure the Bot**
1. Open `discord_bot/casino_bot.py`.
2. **Replace the placeholder** with your actual Discord Bot Token:
   ```python
   BOT_TOKEN = "YOUR_BOT_TOKEN_HERE" 
   ```
3. The script will automatically update the `DB_PASSWORD` to `goatgang_password` after running `setup.sh`.

### **Step 3.2: Install Dependencies**
The bot requires `discord.py` and `mysql-connector-python`.
```bash
pip install discord.py mysql-connector-python
```

### **Step 3.3: Run the Bot**
```bash
python3 discord_bot/casino_bot.py
```

### **Bot Commands**
| Command | Description | Usage |
| :--- | :--- | :--- |
| `!stats` | Shows real-time casino statistics (Total Wagered, Players, Games). | `!stats` |
| `!balance` | Checks a player's balance. | `!balance <player_name>` |
| `!setbalance` | **[ADMIN]** Sets a player's balance. | `!setbalance <player_name> <amount>` |
| `!recentwins` | Shows the last 5 big wins. | `!recentwins` |

---

## 4. iKingSnipe Casino Script Setup (DreamBot)

### **Step 4.1: Install the Script**
1. Copy the standalone JAR file: `ikingsnipe-14.0.0-GOATGANG.jar`
2. Paste it into your DreamBot scripts folder:
   - Windows: `C:\Users\YourName\DreamBot\Scripts\`
   - Mac/Linux: `~/DreamBot/Scripts/`

### **Step 4.2: Configure the Script**
1. Start the script in the DreamBot client.
2. Enter the master password: `sheba777`
3. Navigate to the **Security** tab and **change the master password immediately**.
4. Navigate to the **Trade Config** and **Discord Config** tabs.
5. **Database Settings**: Ensure the settings match your MySQL setup (Host: `localhost`, User: `root`, Pass: `goatgang_password`).
6. **Discord Settings**: The webhook URL is pre-configured, but you can change it here.
7. Configure all other settings (Betting Limits, Games, Humanization).
8. Click **SAVE CONFIG** to persist your settings.

### **Step 4.3: Launch the Casino**
1. Click the **LAUNCH GOATGANG** button.
2. The script will connect to the database and begin accepting trades and running games.

---

## 5. Deployment to DreamBot SDN

If you wish to deploy the script to the DreamBot Script Distribution Network (SDN), use the provided deployment script.

### **Execution**
1. Ensure you have SSH access configured for your SDN repository.
2. Run the deployment script:
   ```bash
   ./deploy_sdn.sh
   ```
3. This script will:
   - Clean and rebuild the project.
   - Copy the JAR to the `sdn_release` folder.
   - Commit the change.
   - Push the new version to your SDN remote.

---

## ⚠️ Security and Maintenance

- **Change Passwords**: Immediately change the default MySQL root password (`goatgang_password`) and the script's master password (`sheba777`).
- **Bot Token**: Keep your Discord Bot Token secure and never share it publicly.
- **Database Backup**: Regularly back up your `goatgang` database to prevent loss of player balances and history.
- **Updates**: When updating the script, ensure you rebuild the JAR and re-deploy the Discord Bot if its logic has changed.
