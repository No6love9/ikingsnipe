# 🪟 Windows Setup Guide: iKingSnipe GoatGang Edition

This guide provides a step-by-step walkthrough for setting up the environment on a Windows machine.

## 1. Install Required Software

| Software | Version | Download Link |
| :--- | :--- | :--- |
| **Java JDK** | 11 or 17 | [Adoptium Temurin](https://adoptium.net/) |
| **Python** | 3.8+ | [Python.org](https://www.python.org/downloads/) |
| **MySQL** | 8.0+ | [MySQL Installer](https://dev.mysql.com/downloads/installer/) |
| **DreamBot** | Latest | [DreamBot.org](https://dreambot.org/download/) |

> **Note**: During Python installation, ensure you check the box **"Add Python to PATH"**.

## 2. Database Configuration

1.  Open **MySQL Workbench** or your preferred SQL client.
2.  Connect to your local instance.
3.  Open the file `database/init_db.sql` from the repository.
4.  Execute the entire script to create the `goatgang` database and required tables.

## 3. Automated Setup

We have provided a batch script to automate the heavy lifting:

1.  Open the `ikingsnipe` folder in File Explorer.
2.  Double-click **`setup_windows.bat`**.
3.  The script will:
    *   Verify your Java and Python installations.
    *   Install all Python dependencies.
    *   Build the Java project into a JAR file.

## 4. Environment Variables

1.  Open the `.env` file created by the setup script.
2.  Update the following fields:
    *   `DISCORD_BOT_TOKEN`: Your bot's token.
    *   `DB_PASS`: Your MySQL root password.
    *   `DISCORD_WEBHOOK_URL`: Your channel's webhook.

## 5. Running the System

### A. The Discord Bot
Open a command prompt in the `ikingsnipe` folder and run:
```cmd
python discord_bot/casino_bot.py
```

### B. The DreamBot Script
1.  Copy `build/libs/ikingsnipe-14.0.jar` to `%USERPROFILE%\DreamBot\Scripts\`.
2.  Launch DreamBot and start the **"GoatGang Edition"** script.
3.  Enter your `MASTER_PASSWORD` (default: `sheba777`) in the GUI.

---
*For enterprise support, visit the GoatGang Discord.*
