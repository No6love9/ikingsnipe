@echo off
echo ============================================
echo  Titan Casino v14.0 - Windows Builder
echo ============================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found! Install from https://www.java.com/
    pause
    exit /b 1
)

echo [1/5] Checking DreamBot JAR...
if not exist "libs\*.jar" (
    echo ERROR: DreamBot client JAR not found in libs\ folder!
    echo.
    echo Please copy client-X.X.X.jar from:
    echo   %%USERPROFILE%%\.dreambot\cache\
    echo to:
    echo   %CD%\libs\
    echo.
    pause
    exit /b 1
)

echo [2/5] Creating directories...
if not exist "output" mkdir output
if exist "build" rmdir /s /q build

echo [3/5] Finding DreamBot JAR...
for %%f in (libs\*.jar) do set DREAMBOT_JAR=%%f
echo Using: %DREAMBOT_JAR%

echo [4/5] Compiling Java source...
javac -encoding UTF-8 -source 1.8 -target 1.8 ^
      -cp "%DREAMBOT_JAR%" ^
      -d build ^
      src\main\java\com\ikingsnipe\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Check the errors above.
    pause
    exit /b 1
)

echo [5/5] Creating JAR file...
cd build
jar cvfm ..\output\TitanCasino-14.0.0.jar ..\MANIFEST.MF com\ikingsnipe\*.class
if errorlevel 1 (
    echo Creating JAR without manifest...
    jar cvf ..\output\TitanCasino-14.0.0.jar com\ikingsnipe\*.class
)
cd ..

echo.
echo ============================================
echo  BUILD SUCCESSFUL!
echo ============================================
echo.
echo Output: output\TitanCasino-14.0.0.jar
echo Size: 
dir output\TitanCasino-14.0.0.jar | find "TitanCasino"
echo.
echo Next: Run INSTALL.bat to install to DreamBot
echo.
pause
