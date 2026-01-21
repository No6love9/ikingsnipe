@echo off
SETLOCAL EnableDelayedExpansion
TITLE iKingSnipe GoatGang Edition - Automated Setup

echo ======================================================
echo    🐐 iKingSnipe GoatGang Edition - Windows Setup
echo ======================================================
echo.

:: Check for Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH.
    echo Please install Python from https://www.python.org/
    pause
    exit /b
)
echo [OK] Python detected.

:: Check for Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install JDK 11 or higher.
    pause
    exit /b
)
echo [OK] Java detected.

:: Create .env from .env.example if it doesn't exist
if not exist .env (
    echo [INFO] Creating .env file from template...
    copy .env.example .env
    echo [ACTION] Please edit the .env file with your credentials before running the bot.
) else (
    echo [OK] .env file already exists.
)

:: Install Python dependencies
echo [INFO] Installing Python dependencies...
pip install -r requirements.txt
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install Python dependencies.
    pause
    exit /b
)
echo [OK] Dependencies installed.

:: Build the project using Gradle
echo [INFO] Building the Java project...
call gradlew.bat build
if %errorlevel% neq 0 (
    echo [ERROR] Build failed. Please check the logs.
    pause
    exit /b
)
echo [OK] Build successful. JAR located in build\libs\

:: Final Instructions
echo.
echo ======================================================
echo    ✅ SETUP COMPLETE
echo ======================================================
echo 1. Edit the .env file with your Discord and DB info.
echo 2. Run 'database\init_db.sql' in your MySQL client.
echo 3. Copy the JAR from build\libs\ to your DreamBot Scripts folder.
echo 4. Run 'python discord_bot\casino_bot.py' to start the Discord bot.
echo ======================================================
pause
