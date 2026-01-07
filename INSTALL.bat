@echo off
echo ============================================
echo  Titan Casino v14.0 - Installer
echo ============================================
echo.

if not exist "output\TitanCasino-14.0.0.jar" (
    echo ERROR: TitanCasino-14.0.0.jar not found!
    echo Please run BUILD.bat first.
    echo.
    pause
    exit /b 1
)

set DREAMBOT_SCRIPTS=%USERPROFILE%\.dreambot\scripts

echo [1/2] Creating DreamBot scripts directory...
if not exist "%DREAMBOT_SCRIPTS%" mkdir "%DREAMBOT_SCRIPTS%"

echo [2/2] Installing JAR to DreamBot...
copy /Y "output\TitanCasino-14.0.0.jar" "%DREAMBOT_SCRIPTS%\"

if errorlevel 1 (
    echo ERROR: Failed to copy JAR!
    pause
    exit /b 1
)

echo.
echo ============================================
echo  INSTALLATION SUCCESSFUL!
echo ============================================
echo.
echo Installed to: %DREAMBOT_SCRIPTS%\TitanCasino-14.0.0.jar
echo.
echo Next steps:
echo 1. Open DreamBot
echo 2. Click "Scripts"
echo 3. Find "Titan Casino v14.0"
echo 4. Click "Start"
echo.
echo Your casino is ready!
echo.
pause
