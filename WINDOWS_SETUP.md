# 🎰 iKingSnipe Titan Casino - Windows Setup Guide

**Super simple setup for Windows users!**

---

## 🚀 Quick Start (3 Clicks!)

### 1. First Time Setup
Double-click: **`SETUP.bat`**

This will:
- Check if Java is installed
- Check for DreamBot client JAR
- Create necessary folders

### 2. Build the Casino
Double-click: **`BUILD.bat`**

This will:
- Compile the Java code
- Create `TitanCasino-2.0.0.jar`

### 3. Install to DreamBot
Double-click: **`INSTALL.bat`**

This will:
- Copy the JAR to DreamBot scripts folder
- Make it ready to run

**Done! Open DreamBot and start your casino!** 🎉

---

## 📋 Prerequisites

### 1. Java (Required)
- **Download**: https://www.java.com/en/download/
- **Version**: Java 8 or higher
- **Check**: Open PowerShell and type `java -version`

### 2. DreamBot Client JAR (Required)
- **Download DreamBot**: https://dreambot.org/download.php
- **Run it once** to generate the client JAR
- **Find the JAR**: 
  - Location: `C:\Users\YourName\.dreambot\cache\`
  - File: `client-X.X.X.jar` (e.g., `client-3.1.0.jar`)
- **Copy to**: `libs\` folder in this project

---

## 📁 Project Structure

```
ikingsnipe\
├── SETUP.bat              ← Run this first!
├── BUILD.bat              ← Run this to compile
├── INSTALL.bat            ← Run this to install
├── libs\
│   └── client-X.X.X.jar   ← Put DreamBot JAR here
├── src\
│   └── main\java\com\ikingsnipe\
│       └── ikingsnipe.java
└── output\
    └── TitanCasino-2.0.0.jar  ← Your compiled script
```

---

## 🔧 Detailed Instructions

### Step 1: Get DreamBot Client JAR

1. **Download DreamBot**:
   ```
   https://dreambot.org/download.php
   ```

2. **Install and run** DreamBot at least once

3. **Close DreamBot**

4. **Find the client JAR**:
   - Press `Win + R`
   - Type: `%USERPROFILE%\.dreambot\cache`
   - Press Enter
   - Look for `client-X.X.X.jar`

5. **Copy to libs folder**:
   - Copy that JAR file
   - Paste it into the `libs\` folder in this project

### Step 2: Run Setup

1. Double-click **`SETUP.bat`**
2. Follow the on-screen instructions
3. It will verify everything is ready

### Step 3: Build the Script

1. Double-click **`BUILD.bat`**
2. Wait for compilation (takes ~5 seconds)
3. You'll see "BUILD SUCCESSFUL!"
4. Output will be in: `output\TitanCasino-2.0.0.jar`

### Step 4: Install to DreamBot

1. Double-click **`INSTALL.bat`**
2. It will copy the JAR to DreamBot scripts folder
3. You'll see "INSTALLATION SUCCESSFUL!"

### Step 5: Run in DreamBot

1. **Open DreamBot**
2. **Login** to your account
3. Click **"Scripts"** button
4. Find **"iKingSnipe TITAN v13.0 FINAL"**
5. Click **"Start"**
6. **Configure** via the GUI that appears
7. **Enjoy your casino!** 🎰

---

## 🎮 What You Get

### 13 Casino Games
1. **Dice Duel** - Classic dice rolling
2. **Flower Poker** - Flower-based poker
3. **Blackjack** - 21 card game
4. **Roulette** - Wheel spinning
5. **Craps** - Dice game
6. **Slots** - Slot machine
7. **High-Low** - Guess higher/lower
8. **Coin Flip** - Heads or tails
9. **Lucky 7** - Roll a 7 to win
10. **Hot Dice** - Hot/cold dice
11. **55x2** - Double or nothing
12. **Poker Dice** - Poker with dice
13. **Custom** - Configurable games

### Admin Features
- Real-time statistics dashboard
- Player balance management
- Blacklist system
- Game configuration
- Discord webhook integration
- Emergency controls

### Technical Features
- Provably fair RNG (HMAC-SHA256)
- SQLite database with backups
- Anti-detection engine
- Session management
- Error recovery

---

## 🔍 Troubleshooting

### "Java is not installed"
**Solution**:
1. Download Java from: https://www.java.com/
2. Install it
3. Restart your computer
4. Run `SETUP.bat` again

### "No DreamBot client JAR found"
**Solution**:
1. Make sure you ran DreamBot at least once
2. Check: `C:\Users\YourName\.dreambot\cache\`
3. Look for `client-X.X.X.jar`
4. Copy it to the `libs\` folder
5. Run `SETUP.bat` again

### "Compilation failed"
**Solution**:
1. Make sure the DreamBot JAR is in `libs\`
2. Make sure Java is installed
3. Try deleting the `output` folder
4. Run `BUILD.bat` again

### "Script doesn't appear in DreamBot"
**Solution**:
1. Make sure you ran `INSTALL.bat`
2. Check: `C:\Users\YourName\.dreambot\scripts\`
3. The JAR should be there
4. Restart DreamBot
5. Click "Refresh" in the scripts menu

### "BUILD.bat closes immediately"
**Solution**:
1. Right-click `BUILD.bat`
2. Choose "Edit"
3. Check if there are any syntax errors
4. Or open PowerShell in this folder and run:
   ```
   .\BUILD.bat
   ```

---

## 📝 Manual Build (If Batch Files Don't Work)

If the batch files don't work, you can build manually:

### 1. Open PowerShell in this folder
- Hold `Shift` + Right-click in the folder
- Choose "Open PowerShell window here"

### 2. Set the classpath
```powershell
$jar = Get-ChildItem libs\*.jar | Select-Object -First 1
```

### 3. Compile
```powershell
javac -encoding UTF-8 -source 1.8 -target 1.8 -cp "libs\$($jar.Name)" -d output src\main\java\com\ikingsnipe\*.java
```

### 4. Create JAR
```powershell
cd output
jar cvf TitanCasino-2.0.0.jar com\ikingsnipe\*.class
cd ..
```

### 5. Install
```powershell
Copy-Item output\TitanCasino-2.0.0.jar $env:USERPROFILE\.dreambot\scripts\
```

---

## 🎯 Quick Command Reference

### Check Java Version
```cmd
java -version
```

### Find DreamBot Cache
```cmd
explorer %USERPROFILE%\.dreambot\cache
```

### Find DreamBot Scripts Folder
```cmd
explorer %USERPROFILE%\.dreambot\scripts
```

### Manual Compile (PowerShell)
```powershell
javac -cp "libs\client-3.1.0.jar" -d output src\main\java\com\ikingsnipe\*.java
```

### Manual JAR Creation (PowerShell)
```powershell
cd output
jar cvf TitanCasino-2.0.0.jar com\ikingsnipe\*.class
```

---

## ⚠️ Important Notes

### First Time Users
- **Run SETUP.bat first** - It checks everything
- **Get the DreamBot JAR** - Required for compilation
- **Java 8+ required** - Won't work without it

### Building
- **BUILD.bat** compiles the code
- Takes about 5-10 seconds
- Creates JAR in `output\` folder

### Installing
- **INSTALL.bat** copies to DreamBot
- Automatic installation
- No manual copying needed

### Running
- Open DreamBot first
- Then select the script
- Configure via GUI

---

## 📧 Support

- **GitHub**: https://github.com/No6love9/ikingsnipe
- **Issues**: https://github.com/No6love9/ikingsnipe/issues

---

## 🎉 You're Ready!

Just follow the 3 steps:
1. **SETUP.bat** - First time setup
2. **BUILD.bat** - Compile the script
3. **INSTALL.bat** - Install to DreamBot

Then open DreamBot and start your casino! 🎰

---

**Made with ❤️ for Windows users**
