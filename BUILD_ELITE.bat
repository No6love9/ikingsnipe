@echo off
echo Building Elite Titan Casino...
call gradlew.bat clean build
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build Successful!
    echo JAR created in output\TitanCasino.jar
) else (
    echo.
    echo Build Failed!
)
pause
