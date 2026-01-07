@echo off
echo ============================================
echo  iKingSnipe Titan Casino - Auto Installer
echo ============================================
echo.

if not exist "output\TitanCasino-2.0.0.jar" (
    echo ERROR: TitanCasino-2.0.0.jar not found!
    echo Please run BUILD.bat first to compile the script.
    echo.
    pause
    exit /b 1
)

set DREAMBOT_SCRIPTS=%USERPROFILE%\.dreambot\scripts

echo [1/2] Creating DreamBot scripts directory...
if not exist "%DREAMBOT_SCRIPTS%" mkdir "%DREAMBOT_SCRIPTS%"

echo [2/2] Copying JAR to DreamBot scripts folder...
copy /Y "output\TitanCasino-2.0.0.jar" "%DREAMBOT_SCRIPTS%\"

if errorlevel 1 (
    echo ERROR: Failed to copy JAR file!
    pause
    exit /b 1
)

echo.
echo ============================================
echo  INSTALLATION SUCCESSFUL!
echo ============================================
echo.
echo Installed to: %DREAMBOT_SCRIPTS%\TitanCasino-2.0.0.jar
echo.
echo Next steps:
echo 1. Open DreamBot
echo 2. Click "Scripts" menu
echo 3. Find "iKingSnipe TITAN v13.0 FINAL"
echo 4. Click "Start"
echo.
echo Your casino is ready to run!
echo.
pause
