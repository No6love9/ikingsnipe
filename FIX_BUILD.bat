@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   snipes♧scripts - Ultimate Build Fixer
echo ===================================================

:: 1. Check if user dragged a folder onto the script
if not "%~1"=="" (
    if exist "%~1\bin\javac.exe" (
        set "JDK_PATH=%~1"
        echo [+] Using dragged JDK path: !JDK_PATH!
        goto :START_BUILD
    )
)

:: 2. Try to find JDK automatically
echo [*] Searching for JDK...
set "JDK_PATH="
for /d %%i in ("C:\Program Files\Java\jdk*") do (
    if exist "%%i\bin\javac.exe" set "JDK_PATH=%%i"
)

if "%JDK_PATH%"=="" (
    echo [!] ERROR: No JDK found in C:\Program Files\Java\
    echo.
    echo HOW TO FIX:
    echo 1. Download JDK 11 or 17 from https://adoptium.net/
    echo 2. Install it.
    echo 3. If this script still fails, DRAG the JDK folder 
    echo    (e.g. C:\Program Files\Java\jdk-11.x.x) 
    echo    directly onto this .bat file.
    echo.
    pause
    exit /b 1
)

:START_BUILD
echo [+] Found JDK at: %JDK_PATH%
set "JAVA_HOME=%JDK_PATH%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [+] Current Java Version:
java -version
echo.

echo [+] Running Build...
:: Use --no-daemon to avoid background process issues
call gradlew.bat clean shadowJar --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo [SUCCESS] Build complete! 
    echo Your jar is in: build\libs\
    echo ===================================================
) else (
    echo.
    echo [!] Build failed. 
    echo If you see 'tools.jar' error, it means you are using a JRE, not a JDK.
    echo Make sure the folder contains a 'bin' folder with 'javac.exe' inside.
)

pause
