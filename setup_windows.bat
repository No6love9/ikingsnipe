@echo off
SETLOCAL EnableDelayedExpansion
TITLE iKingSnipe GoatGang Edition - Hardened Setup

:: ======================================================
::    🐐 iKingSnipe GoatGang Edition - Hardened Setup
:: ======================================================
:: Version: 2.0 (Operator-Grade)
:: Purpose: Idempotent environment configuration and build
:: ======================================================

echo [INFO] Starting environment verification...

:: 1. Check for Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python not found. Install from https://www.python.org/
    pause && exit /b 1
)
echo [OK] Python detected.

:: 2. Check for Java 11+ (Required for HikariCP 5.x)
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JVER=%%g
    set JVER=!JVER:"=!
    for /f "delims=. tokens=1" %%v in ("!JVER!") do set JMAJOR=%%v
)

if !JMAJOR! LSS 11 (
    echo [ERROR] Java version !JVER! detected. Java 11 or higher is REQUIRED.
    echo [ACTION] Please install Temurin 11: https://adoptium.net/temurin/releases/?version=11
    pause && exit /b 1
)
echo [OK] Java !JVER! detected (Major: !JMAJOR!).

:: 3. Idempotent .env Creation
if not exist .env (
    echo [INFO] Initializing .env from template...
    copy .env.example .env >nul
    echo [WARN] Edit .env with your credentials before running the bot.
) else (
    echo [OK] .env configuration found.
)

:: 4. Dependency Management
echo [INFO] Syncing Python dependencies...
python -m pip install --upgrade pip >nul
pip install -r requirements.txt --quiet
if %errorlevel% neq 0 (
    echo [ERROR] Dependency sync failed.
    pause && exit /b 1
)
echo [OK] Python environment ready.

:: 5. Gradle Toolchain Build
echo [INFO] Executing Gradle build (ShadowJar)...
call gradlew.bat clean shadowJar --no-daemon
if %errorlevel% neq 0 (
    echo [ERROR] Build failed. Check logs above.
    pause && exit /b 1
)

:: 6. Deployment Preparation
set JAR_NAME=ikingsnipe-14.0.0-GOATGANG.jar
if exist build\libs\!JAR_NAME! (
    echo [SUCCESS] Build complete: build\libs\!JAR_NAME!
    echo [INFO] Copy this JAR to: %%USERPROFILE%%\DreamBot\Scripts\
)

echo.
echo ======================================================
echo    ✅ SYSTEM READY - GOATGANG OPERATIONAL
echo ======================================================
pause
