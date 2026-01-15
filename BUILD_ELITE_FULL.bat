@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Elite Titan Casino - Full Build Orchestrator
echo ===================================================
echo.

REM 1. Detect JDK 21 under Eclipse Adoptium
set JDK21_DIR=
for d %%i in (CProgram FilesEclipse Adoptiumjdk-21) do (
    if exist %%ibinjavac.exe set JDK21_DIR=%%i
)

if %JDK21_DIR%== (
    echo [!] No JDK 21 found under
    echo     CProgram FilesEclipse Adoptium
    echo.
    echo Install Temurin JDK 21 (LTS) from
    echo     httpsadoptium.net
    echo Then re-run this script.
    echo.
    pause
    exit b 1
)

echo [+] Detected JDK 21 at %JDK21_DIR%
echo.

REM 2. Ensure we are in project root
cd d %~dp0
echo [+] Project root %cd%
echo.

REM 3. Patch gradle.properties with org.gradle.java.home
set GRADLE_PROPS=gradle.properties
set JAVA_HOME_LINE=org.gradle.java.home=%JDK21_DIR=%

if exist %GRADLE_PROPS% (
    %GRADLE_PROPS%.tmp (
        set FOUND_LINE=
        for f usebackq delims= %%L in (%GRADLE_PROPS%) do (
            echo %%L  findstr r c^org.gradle.java.home= nul
            if not errorlevel 1 (
                echo %JAVA_HOME_LINE%
                set FOUND_LINE=1
            ) else (
                echo %%L
            )
        )
        if not defined FOUND_LINE (
            echo %JAVA_HOME_LINE%
        )
    )
    move y %GRADLE_PROPS%.tmp %GRADLE_PROPS% nul
    echo [+] Updated gradle.properties with
    echo     %JAVA_HOME_LINE%
) else (
    echo %JAVA_HOME_LINE%%GRADLE_PROPS%
    echo [+] Created gradle.properties with
    echo     %JAVA_HOME_LINE%
)
echo.

REM 4. Run Gradle with JDK 21
echo [+] Using JDK 21 for Gradle
echo     %JDK21_DIR%
echo.

set ORG_GRADLE_JAVA_HOME=%JDK21_DIR%
set JAVA_HOME=%JDK21_DIR%
set PATH=%JDK21_DIR%bin;%PATH%

echo [+] Gradle JVM check
call gradlew.bat --version
echo.

echo [+] Running build clean shadowJar --no-daemon
echo.
call gradlew.bat clean shadowJar --no-daemon

echo.
echo ===================================================
echo   Build process finished.
echo ===================================================
echo.
pause
endlocal
