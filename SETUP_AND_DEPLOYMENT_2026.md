# iKingSnipe Setup & Deployment Guide 2026

**Version:** 14.0.0 - GoatGang Edition  
**Last Updated:** January 2026  
**Compatibility:** DreamBot 3/4, Java 8-11, Windows 10/11

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Java Installation](#java-installation)
4. [Building from Source](#building-from-source)
5. [Deployment to DreamBot](#deployment-to-dreambot)
6. [Troubleshooting](#troubleshooting)
7. [Advanced Configuration](#advanced-configuration)

---

## 🚀 Quick Start

### For Experienced Users (5 minutes)

```powershell
# 1. Install Java
.\Install-JavaDependencies.ps1

# 2. Build project
.\gradlew clean build

# 3. Deploy to DreamBot
.\Deploy-ToDreamBot.ps1
```

### For New Users (15 minutes)

Follow the step-by-step guide below.

---

## 📦 Prerequisites

### System Requirements

- **OS:** Windows 10 or Windows 11
- **RAM:** Minimum 4GB (8GB recommended)
- **Disk Space:** 2GB free
- **DreamBot:** Version 3 or 4 installed

### Software Requirements

- **Java JDK:** 8 (for DreamBot) and 11 (for compilation)
- **PowerShell:** 5.0 or higher (built-in on Windows 10/11)
- **Git:** (optional, for cloning repository)

### Check Your System

```powershell
# Check Windows version
[System.Environment]::OSVersion.VersionString

# Check PowerShell version
$PSVersionTable.PSVersion

# Check if Java is installed
java -version
javac -version
```

---

## ☕ Java Installation

### Method 1: Automated Installation (Recommended)

The `Install-JavaDependencies.ps1` script handles everything automatically.

#### Step 1: Open PowerShell as Administrator

1. Press `Win + X`
2. Select "Windows PowerShell (Admin)" or "Terminal (Admin)"
3. Accept the User Account Control prompt

#### Step 2: Navigate to Project Directory

```powershell
cd C:\path\to\ikingsnipe
```

#### Step 3: Run the Installation Script

```powershell
# Install both Java 8 and 11
.\Install-JavaDependencies.ps1

# Or install specific version
.\Install-JavaDependencies.ps1 -JavaVersion 11

# Or force reinstall
.\Install-JavaDependencies.ps1 -Force
```

#### Step 4: Restart Your Terminal

Close and reopen PowerShell to reload environment variables.

#### Step 5: Verify Installation

```powershell
# Check Java 8
java -version

# Check Java 11
javac -version

# Check environment variables
echo $env:JAVA_HOME
echo $env:JAVA8_HOME
echo $env:JAVA11_HOME
```

### Method 2: Manual Installation

If the automated script fails, install manually:

1. **Download Java 8 (JDK)**
   - Visit: https://adoptium.net/temurin/releases/?version=8
   - Download: Windows x64 JDK
   - Install to: `C:\Program Files\Java\jdk-8`

2. **Download Java 11 (JDK)**
   - Visit: https://adoptium.net/temurin/releases/?version=11
   - Download: Windows x64 JDK
   - Install to: `C:\Program Files\Java\jdk-11`

3. **Set Environment Variables**
   - Press `Win + X`, select "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Add new User variables:
     - `JAVA8_HOME`: `C:\Program Files\Java\jdk-8`
     - `JAVA11_HOME`: `C:\Program Files\Java\jdk-11`
     - `JAVA_HOME`: `C:\Program Files\Java\jdk-11`
   - Edit `PATH`, add: `C:\Program Files\Java\jdk-11\bin`

4. **Verify Installation**
   ```powershell
   java -version
   javac -version
   ```

---

## 🔨 Building from Source

### Prerequisites

- Java JDK 11 installed and configured
- `JAVA_HOME` environment variable set

### Build Steps

#### Option 1: Using Gradle Wrapper (Recommended)

```powershell
# Navigate to project directory
cd C:\path\to\ikingsnipe

# Clean and build
.\gradlew clean build

# Or build with tests
.\gradlew clean build --refresh-dependencies
```

#### Option 2: Using Build Batch File

```powershell
# Run the batch file
.\BUILD.bat
```

### Build Output

After successful build, you'll find:

- **Main JAR**: `build\libs\ikingsnipe-14.0.0-GOATGANG.jar`
- **Output JAR**: `output\ikingsnipe-14.0.0-GOATGANG.jar`
- **Compatibility JAR**: `output\EliteTitanCasino.jar`

### Build Troubleshooting

#### Error: "Java not found"

```powershell
# Check Java installation
java -version

# If not found, restart PowerShell and try again
# If still not found, reinstall Java using Install-JavaDependencies.ps1
```

#### Error: "Gradle wrapper not found"

```powershell
# Ensure you're in the correct directory
cd C:\path\to\ikingsnipe

# Check if gradlew.bat exists
dir gradlew.bat

# If missing, clone repository again
```

#### Error: "Compilation failed"

```powershell
# Clean and rebuild with dependencies
.\gradlew clean build --refresh-dependencies

# Or check for deprecated API usage
.\gradlew build -x test
```

---

## 📤 Deployment to DreamBot

### Automatic Deployment (Recommended)

The `Deploy-ToDreamBot.ps1` script automates the entire process.

#### Step 1: Open PowerShell as Administrator

```powershell
# Navigate to project directory
cd C:\path\to\ikingsnipe
```

#### Step 2: Run Deployment Script

```powershell
# Auto-detect DreamBot and deploy
.\Deploy-ToDreamBot.ps1

# Or specify DreamBot path manually
.\Deploy-ToDreamBot.ps1 -DreamBotPath "C:\DreamBot"

# Or skip auto-build
.\Deploy-ToDreamBot.ps1 -AutoBuild $false
```

#### Step 3: Verify Deployment

```powershell
# Check if JAR was deployed
dir $env:USERPROFILE\DreamBot\Scripts\ikingsnipe-14.0.0-GOATGANG.jar
```

### Manual Deployment

If automatic deployment fails:

1. **Locate DreamBot Scripts Folder**
   - Default: `C:\Users\YourUsername\DreamBot\Scripts\`
   - Or: `C:\Users\YourUsername\AppData\Local\DreamBot\Scripts\`

2. **Copy JAR File**
   ```powershell
   # Copy from output folder
   Copy-Item "output\ikingsnipe-14.0.0-GOATGANG.jar" `
             "$env:USERPROFILE\DreamBot\Scripts\" -Force
   ```

3. **Verify in DreamBot**
   - Open DreamBot Client
   - Go to Scripts Manager
   - Search for "GoatGang" or "ikingsnipe"
   - Script should appear in the list

### Post-Deployment Setup

#### Step 1: Load Script in DreamBot

1. Open DreamBot Client
2. Navigate to **Scripts Manager**
3. Search for **"GoatGang"** or **"ikingsnipe"**
4. Click **"Load Script"**

#### Step 2: Configure Settings

A GUI window will appear with these options:

| Setting | Description | Default |
|---------|-------------|---------|
| **Game Mode** | Select: Dice, Wheel, Roulette, Craps | Dice |
| **Bet Amount** | Initial bet in GP | 1,000,000 |
| **Min Trade** | Minimum trade to accept | 1,000,000 |
| **Ad Message** | Chat advertisement message | "Elite Casino \| Fast Payouts..." |
| **Win Message** | Message when player wins | "Congratulations! You won!" |
| **Loss Message** | Message when player loses | "Better luck next time!" |
| **Auto Accept** | Automatically accept valid trades | ✅ Enabled |
| **Double or Nothing** | Offer re-bet after wins | ✅ Enabled |

#### Step 3: Start Script

1. Click **"Start Script"**
2. Script will initialize and begin advertising
3. Monitor the paint overlay for status

---

## 🔧 Troubleshooting

### Script Not Appearing in DreamBot

**Problem:** Script doesn't show up in Scripts Manager

**Solutions:**
1. Restart DreamBot Client completely
2. Verify JAR is in correct folder:
   ```powershell
   dir "$env:USERPROFILE\DreamBot\Scripts\ikingsnipe-14.0.0-GOATGANG.jar"
   ```
3. Check JAR file size (should be 20-50 MB)
4. Re-run deployment script:
   ```powershell
   .\Deploy-ToDreamBot.ps1 -Force
   ```

### Script Crashes on Load

**Problem:** Script loads but crashes immediately

**Solutions:**
1. Check DreamBot logs for errors
2. Verify Java 8 is installed:
   ```powershell
   java -version
   ```
3. Ensure DreamBot is fully updated
4. Try rebuilding and redeploying:
   ```powershell
   .\gradlew clean build
   .\Deploy-ToDreamBot.ps1
   ```

### "Java not found" Error

**Problem:** PowerShell can't find Java

**Solutions:**
1. Restart PowerShell (critical!)
2. Verify Java installation:
   ```powershell
   Get-Command java
   echo $env:JAVA_HOME
   ```
3. Reinstall Java:
   ```powershell
   .\Install-JavaDependencies.ps1 -Force
   ```

### Build Fails with Deprecation Warnings

**Problem:** Build completes but with warnings about deprecated APIs

**Solutions:**
1. These are normal warnings and don't prevent execution
2. To suppress warnings:
   ```powershell
   .\gradlew build -x test
   ```
3. To fix deprecated code (advanced):
   - Edit source files to use newer APIs
   - See `BUG_FIXES_v14.md` for details

### DreamBot Scripts Folder Not Found

**Problem:** Script can't locate DreamBot installation

**Solutions:**
1. Manually specify path:
   ```powershell
   .\Deploy-ToDreamBot.ps1 -DreamBotPath "C:\Your\DreamBot\Path"
   ```
2. Find DreamBot installation:
   ```powershell
   Get-ChildItem -Path "$env:USERPROFILE" -Recurse -Filter "DreamBot" -Directory
   ```
3. Check common locations:
   - `C:\Users\YourName\DreamBot`
   - `C:\Users\YourName\AppData\Local\DreamBot`
   - `C:\Program Files\DreamBot`

---

## ⚙️ Advanced Configuration

### Custom Build Configuration

Edit `build.gradle` to customize:

```gradle
// Change output JAR name
shadowJar {
    archiveFileName = 'MyCustomName.jar'
}

// Add custom dependencies
dependencies {
    implementation 'com.example:library:1.0.0'
}

// Modify Java version
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

### Environment Variables

Set these for advanced configuration:

```powershell
# Use specific Java version
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"

# Gradle memory configuration
$env:GRADLE_OPTS = "-Xmx2g"

# DreamBot configuration
$env:DREAMBOT_PATH = "C:\Users\YourName\DreamBot"
```

### Performance Tuning

For faster builds:

```powershell
# Enable parallel compilation
.\gradlew build -x test --parallel

# Use daemon for faster subsequent builds
.\gradlew build --daemon

# Increase memory for Gradle
$env:GRADLE_OPTS = "-Xmx4g -XX:+UseG1GC"
```

---

## 📊 Project Structure

```
ikingsnipe/
├── src/main/java/com/ikingsnipe/
│   ├── core/                    # Main application
│   ├── casino/
│   │   ├── games/              # Game implementations
│   │   ├── managers/           # Game managers
│   │   ├── gui/                # GUI components
│   │   └── utils/              # Utilities
│   ├── framework/              # Tree-Branch-Leaf framework
│   └── database/               # Database management
├── libs/                        # DreamBot API JAR
├── build/                       # Build output (generated)
├── output/                      # Final JAR placement
├── build.gradle                 # Gradle configuration
├── Install-JavaDependencies.ps1 # Java installer
├── Deploy-ToDreamBot.ps1        # Deployment script
└── README.md                    # Documentation
```

---

## 🔐 Security Notes

### Safe Practices

1. **Always backup** before deploying to production
2. **Use HTTPS** for all external connections
3. **Validate input** from game players
4. **Encrypt sensitive** data (API keys, credentials)
5. **Monitor logs** for suspicious activity

### DreamBot Security

- Script runs with DreamBot's security context
- All API calls are validated by DreamBot
- Trade verification prevents item swaps
- Automatic timeout protection

---

## 📞 Support & Resources

### Documentation

- **README.md** - Project overview
- **CRAPS_GAME_GUIDE.md** - Craps game documentation
- **BUG_FIXES_v14.md** - Known issues and fixes
- **COMPLETION_SUMMARY.md** - Feature summary

### External Resources

- **DreamBot Docs**: https://dreambot.org/javadocs/
- **Java Docs**: https://docs.oracle.com/javase/11/docs/
- **Gradle Docs**: https://gradle.org/

### Getting Help

1. Check documentation files in repository
2. Review troubleshooting section above
3. Check DreamBot logs for error details
4. Open GitHub issue with error details

---

## ✅ Verification Checklist

Before deploying to production:

- [ ] Java 8 and 11 installed and verified
- [ ] Project builds successfully without errors
- [ ] JAR file exists in output folder
- [ ] JAR file size is reasonable (20-50 MB)
- [ ] Script appears in DreamBot Scripts Manager
- [ ] Script loads without crashing
- [ ] GUI configuration window appears
- [ ] Game logic executes correctly
- [ ] Trade verification works
- [ ] Chat messages send properly
- [ ] Paint overlay displays correctly
- [ ] No deprecation warnings in build output

---

## 🎉 You're Ready!

Once you've completed all steps:

1. ✅ Java is installed
2. ✅ Project is built
3. ✅ JAR is deployed to DreamBot
4. ✅ Script is loaded and configured
5. ✅ Game is running

**Congratulations! Your iKingSnipe casino bot is ready for deployment!**

---

**Last Updated:** January 2026  
**Version:** 14.0.0 - GoatGang Edition  
**Maintained By:** iKingSnipe Development Team
