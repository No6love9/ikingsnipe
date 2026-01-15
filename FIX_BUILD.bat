@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   snipes♧scripts - Build Fixer for Windows
echo ===================================================

:: Try to find JDK in common locations
set "JDK_PATH="
for /d %%i in ("C:\Program Files\Java\jdk*") do (
    set "JDK_PATH=%%i"
)

if "%JDK_PATH%"=="" (
    echo [!] ERROR: No JDK found in C:\Program Files\Java\
    echo Please install a JDK (not just JRE) from https://adoptium.net/
    pause
    exit /b 1
)

echo [+] Found JDK at: %JDK_PATH%
echo [+] Setting JAVA_HOME...
set "JAVA_HOME=%JDK_PATH%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [+] Verifying Java version...
java -version

echo.
echo [+] Starting Build...
call gradlew.bat --no-daemon clean shadowJar

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo [SUCCESS] Build complete! 
    echo Your jar is in: build\libs\
    echo ===================================================
) else (
    echo.
    echo [!] Build failed. Please check the errors above.
)

pause
