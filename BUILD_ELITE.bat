@echo off
setlocal enabledelayedexpansion
color 0A

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo   ELITE TITAN CASINO v15.0 - ULTIMATE BUILD SYSTEM
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo   Features:
echo   - Auto-detects DreamBot JAR
echo   - Robust error handling
echo   - Automatic dependency resolution
echo   - Clean compilation
echo   - Professional JAR packaging
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 1: Check Java
REM ═══════════════════════════════════════════════════════════════════════════

echo [1/8] Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ✗ ERROR: Java not found!
    echo.
    echo Please install Java from: https://www.java.com/
    echo.
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo ✓ Java detected: %JAVA_VERSION%

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 2: Check DreamBot JAR
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [2/8] Checking for DreamBot client JAR...

set DREAMBOT_JAR=
set LIBS_DIR=%CD%\libs

REM Check libs folder
if exist "%LIBS_DIR%\*.jar" (
    for %%f in ("%LIBS_DIR%\*.jar") do (
        set DREAMBOT_JAR=%%f
        goto :found_jar
    )
)

REM Check user's DreamBot cache
set DREAMBOT_CACHE=%USERPROFILE%\.dreambot\cache
if exist "%DREAMBOT_CACHE%\client-*.jar" (
    echo.
    echo ⚠ DreamBot JAR not in libs folder
    echo   Found in cache: %DREAMBOT_CACHE%
    echo.
    echo   Copying to libs folder...
    
    if not exist "%LIBS_DIR%" mkdir "%LIBS_DIR%"
    
    for %%f in ("%DREAMBOT_CACHE%\client-*.jar") do (
        copy "%%f" "%LIBS_DIR%\" >nul 2>&1
        set DREAMBOT_JAR=%LIBS_DIR%\%%~nxf
        echo ✓ Copied: %%~nxf
        goto :found_jar
    )
)

REM Not found anywhere
echo.
echo ✗ ERROR: DreamBot client JAR not found!
echo.
echo Please do ONE of the following:
echo.
echo   Option 1: Copy client-X.X.X.jar to: %LIBS_DIR%\
echo   Option 2: Run DreamBot once, it will be auto-detected
echo.
echo To find the JAR manually:
echo   1. Press Win+R
echo   2. Type: %%USERPROFILE%%\.dreambot\cache
echo   3. Copy client-X.X.X.jar to libs\ folder
echo.
pause
exit /b 1

:found_jar
echo ✓ Using: %DREAMBOT_JAR%

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 3: Clean previous build
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [3/8] Cleaning previous build...

if exist "build" (
    rmdir /s /q "build" >nul 2>&1
    echo ✓ Removed old build directory
)

if exist "output" (
    if exist "output\*.jar" (
        del /q "output\*.jar" >nul 2>&1
        echo ✓ Removed old JAR files
    )
) else (
    mkdir "output"
    echo ✓ Created output directory
)

mkdir "build" >nul 2>&1

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 4: Find source files
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [4/8] Locating source files...

set SRC_DIR=src\main\java
set PACKAGE_DIR=%SRC_DIR%\com\ikingsnipe

if not exist "%PACKAGE_DIR%\EliteTitanCasino.java" (
    echo ✗ ERROR: Source file not found!
    echo   Expected: %PACKAGE_DIR%\EliteTitanCasino.java
    pause
    exit /b 1
)

echo ✓ Found: EliteTitanCasino.java

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 5: Compile Java source
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [5/8] Compiling Java source code...
echo   This may take a moment...

javac -encoding UTF-8 ^
      -source 1.8 ^
      -target 1.8 ^
      -cp "%DREAMBOT_JAR%" ^
      -d build ^
      -Xlint:deprecation ^
      "%PACKAGE_DIR%\EliteTitanCasino.java" 2>build_errors.txt

if errorlevel 1 (
    echo.
    echo ✗ COMPILATION FAILED!
    echo.
    echo Errors:
    type build_errors.txt
    echo.
    echo Common fixes:
    echo   1. Make sure DreamBot JAR is correct version
    echo   2. Check Java version (need 8+)
    echo   3. Verify source code is complete
    echo.
    pause
    exit /b 1
)

del build_errors.txt >nul 2>&1
echo ✓ Compilation successful!

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 6: Create manifest
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [6/8] Creating JAR manifest...

set MANIFEST=build\MANIFEST.MF

echo Manifest-Version: 1.0> "%MANIFEST%"
echo Main-Class: com.ikingsnipe.EliteTitanCasino>> "%MANIFEST%"
echo Implementation-Title: ELITE TITAN CASINO>> "%MANIFEST%"
echo Implementation-Version: 15.0>> "%MANIFEST%"
echo Implementation-Vendor: iKingSnipe>> "%MANIFEST%"
echo Built-By: %USERNAME%>> "%MANIFEST%"
echo Built-Date: %DATE% %TIME%>> "%MANIFEST%"
echo.>> "%MANIFEST%"

echo ✓ Manifest created

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 7: Package JAR
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [7/8] Packaging JAR file...

cd build
jar cvfm ..\output\EliteTitanCasino-15.0.jar MANIFEST.MF com\ikingsnipe\*.class >nul 2>&1

if errorlevel 1 (
    echo ✗ JAR creation failed!
    cd ..
    pause
    exit /b 1
)

cd ..
echo ✓ JAR created successfully!

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 8: Verify output
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo [8/8] Verifying output...

if not exist "output\EliteTitanCasino-15.0.jar" (
    echo ✗ JAR file not found!
    pause
    exit /b 1
)

for %%f in ("output\EliteTitanCasino-15.0.jar") do set JAR_SIZE=%%~zf
set /a JAR_SIZE_KB=JAR_SIZE/1024

echo ✓ JAR file verified
echo   Location: %CD%\output\EliteTitanCasino-15.0.jar
echo   Size: %JAR_SIZE_KB% KB

REM ═══════════════════════════════════════════════════════════════════════════
REM SUCCESS!
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo   BUILD SUCCESSFUL!
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo   Output: output\EliteTitanCasino-15.0.jar
echo   Size: %JAR_SIZE_KB% KB
echo.
echo   Next steps:
echo   1. Run INSTALL_ELITE.bat to install to DreamBot
echo   2. Or manually copy to: %%USERPROFILE%%\.dreambot\scripts\
echo.
echo   Features in this build:
echo   ✓ 13 Complete Casino Games
echo   ✓ Auto-Setup System
echo   ✓ Professional GUI
echo   ✓ Provably Fair RNG
echo   ✓ Robust Error Handling
echo   ✓ Discord Integration
echo   ✓ Auto-Backup System
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

pause
