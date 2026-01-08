@echo off
setlocal enabledelayedexpansion
color 0B

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo   ELITE TITAN CASINO v15.0 - INSTALLER
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Check if JAR exists
if not exist "output\EliteTitanCasino-15.0.jar" (
    echo ✗ ERROR: EliteTitanCasino-15.0.jar not found!
    echo.
    echo Please run BUILD_ELITE.bat first.
    echo.
    pause
    exit /b 1
)

REM Get JAR size
for %%f in ("output\EliteTitanCasino-15.0.jar") do set JAR_SIZE=%%~zf
set /a JAR_SIZE_KB=JAR_SIZE/1024

echo [1/3] Found: EliteTitanCasino-15.0.jar (%JAR_SIZE_KB% KB)

REM Create DreamBot scripts directory
set DREAMBOT_SCRIPTS=%USERPROFILE%\.dreambot\scripts

echo.
echo [2/3] Creating DreamBot scripts directory...

if not exist "%DREAMBOT_SCRIPTS%" (
    mkdir "%DREAMBOT_SCRIPTS%"
    echo ✓ Created: %DREAMBOT_SCRIPTS%
) else (
    echo ✓ Directory exists: %DREAMBOT_SCRIPTS%
)

REM Copy JAR
echo.
echo [3/3] Installing JAR to DreamBot...

copy /Y "output\EliteTitanCasino-15.0.jar" "%DREAMBOT_SCRIPTS%\" >nul 2>&1

if errorlevel 1 (
    echo ✗ ERROR: Failed to copy JAR!
    echo.
    echo Please check:
    echo   1. You have write permissions
    echo   2. DreamBot is not currently running
    echo   3. No antivirus is blocking the operation
    echo.
    pause
    exit /b 1
)

echo ✓ Installed successfully!

REM Success message
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo   INSTALLATION COMPLETE!
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo   Installed to: %DREAMBOT_SCRIPTS%\EliteTitanCasino-15.0.jar
echo   Size: %JAR_SIZE_KB% KB
echo.
echo   Next steps:
echo   1. Open DreamBot client
echo   2. Click "Scripts" button
echo   3. Find "ELITE TITAN CASINO v15.0"
echo   4. Click "Start"
echo   5. Configure in the GUI that appears
echo.
echo   Features:
echo   ✓ 13 Casino Games (Dice, FP, BJ, 55x2, Roulette, Slots, etc.)
echo   ✓ Professional GUI with real-time stats
echo   ✓ Auto-setup on first run
echo   ✓ Provably fair RNG
echo   ✓ Discord webhooks
echo   ✓ Auto-backup system
echo.
echo   Your elite casino is ready to dominate! 🎰
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

pause
