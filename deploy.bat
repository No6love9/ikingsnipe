@echo off
REM DreamBot Script Deployment Script for Windows

REM Configuration
SET SCRIPT_NAME=snipes_scripts_enterprise
SET DREAMBOT_SCRIPTS_DIR=%USERPROFILE%\DreamBot\Scripts
SET BUILD_JAR=build\libs\snipes_scripts_enterprise.jar

echo Building %SCRIPT_NAME%...
call gradlew.bat clean build

REM Check if build was successful
IF %ERRORLEVEL% NEQ 0 (
    echo Build failed. Please check the errors above.
    EXIT /B 1
)

echo Build successful!

REM Create DreamBot scripts directory if it doesn't exist
IF NOT EXIST "%DREAMBOT_SCRIPTS_DIR%" (
    mkdir "%DREAMBOT_SCRIPTS_DIR%"
)

REM Copy the JAR to DreamBot scripts folder
echo Deploying to %DREAMBOT_SCRIPTS_DIR%...
copy /Y %BUILD_JAR% "%DREAMBOT_SCRIPTS_DIR%"

echo Deployment complete! You can now find '%SCRIPT_NAME%' in your DreamBot client.

pause
