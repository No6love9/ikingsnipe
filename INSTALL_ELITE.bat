@echo off
set "DREAMBOT_DIR=%USERPROFILE%\DreamBot\Scripts"
echo Installing to %DREAMBOT_DIR%...

if not exist "%DREAMBOT_DIR%" mkdir "%DREAMBOT_DIR%"

if exist "output\TitanCasino.jar" (
    copy /y "output\TitanCasino.jar" "%DREAMBOT_DIR%\"
    echo.
    echo Installation Complete!
) else (
    echo.
    echo Error: output\TitanCasino.jar not found. Run BUILD_ELITE.bat first.
)
pause
