@echo off
echo ============================================
echo  iKingSnipe Titan Casino - First Time Setup
echo ============================================
echo.
echo This script will help you set up everything needed to build and run the casino.
echo.
pause

echo.
echo [Step 1/3] Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo Java is NOT installed!
    echo.
    echo Please install Java 8 or higher:
    echo https://www.java.com/en/download/
    echo.
    echo After installing Java, run this script again.
    pause
    exit /b 1
) else (
    echo Java is installed! ✓
    java -version
)

echo.
echo [Step 2/3] Checking for DreamBot client JAR...
if exist "libs\*.jar" (
    echo DreamBot JAR found in libs\ folder! ✓
    dir /b libs\*.jar
) else (
    echo.
    echo DreamBot JAR NOT found!
    echo.
    echo Please follow these steps:
    echo.
    echo 1. Download DreamBot from: https://dreambot.org/download.php
    echo 2. Install and run DreamBot at least once
    echo 3. Close DreamBot
    echo 4. Find the client JAR file in one of these locations:
    echo    - %%USERPROFILE%%\.dreambot\cache\client-X.X.X.jar
    echo    - C:\Users\%USERNAME%\.dreambot\cache\client-X.X.X.jar
    echo.
    echo 5. Copy that JAR file to this folder: libs\
    echo.
    echo Example:
    echo    copy "%%USERPROFILE%%\.dreambot\cache\client-3.1.0.jar" libs\
    echo.
    echo After copying the JAR, run this script again.
    pause
    exit /b 1
)

echo.
echo [Step 3/3] Creating directories...
if not exist "output" mkdir output
echo Output directory created! ✓

echo.
echo ============================================
echo  SETUP COMPLETE!
echo ============================================
echo.
echo You're ready to build! Run these commands:
echo.
echo   BUILD.bat    - Compile the casino script
echo   INSTALL.bat  - Install to DreamBot
echo.
echo Or just double-click BUILD.bat to get started!
echo.
pause
