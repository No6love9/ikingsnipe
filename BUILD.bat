@echo off
echo ============================================
echo  iKingSnipe Titan Casino - Windows Builder
echo ============================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 8 or higher from: https://www.java.com/
    pause
    exit /b 1
)

echo [1/4] Checking for DreamBot client JAR...
if not exist "libs\*.jar" (
    echo ERROR: No DreamBot client JAR found in libs\ folder
    echo.
    echo Please follow these steps:
    echo 1. Download DreamBot from: https://dreambot.org/download.php
    echo 2. Run DreamBot once
    echo 3. Find client-X.X.X.jar in: %%USERPROFILE%%\.dreambot\cache\
    echo 4. Copy that JAR file to the libs\ folder in this project
    echo.
    pause
    exit /b 1
)

echo [2/4] Creating output directory...
if not exist "output" mkdir output

echo [3/4] Compiling Java source...
dir /b libs\*.jar > temp_classpath.txt
set /p DREAMBOT_JAR=<temp_classpath.txt
del temp_classpath.txt

javac -encoding UTF-8 -source 1.8 -target 1.8 -cp "libs\%DREAMBOT_JAR%" -d output src\main\java\com\ikingsnipe\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Check the error messages above.
    pause
    exit /b 1
)

echo [4/4] Creating JAR file...
cd output
jar cvf TitanCasino-2.0.0.jar com\ikingsnipe\*.class
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ============================================
echo  BUILD SUCCESSFUL!
echo ============================================
echo.
echo Output: output\TitanCasino-2.0.0.jar
echo.
echo Next steps:
echo 1. Copy output\TitanCasino-2.0.0.jar to %%USERPROFILE%%\.dreambot\scripts\
echo 2. Open DreamBot
echo 3. Click Scripts -^> Find "iKingSnipe TITAN" -^> Start
echo.
pause
