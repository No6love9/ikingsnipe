@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
echo Building Elite Titan Casino...

rem Ensure gradlew exists or run gradle if on PATH
if exist "%~dp0gradlew.bat" (
  set "GRADLEW=%~dp0gradlew.bat"
) else (
  set "GRADLEW=gradle"
)

echo Running %GRADLEW% --no-daemon clean shadowJar
"%GRADLEW%" --no-daemon clean shadowJar
if errorlevel 1 (
  echo shadowJar task failed or not available; attempting standard jar build...
  "%GRADLEW%" --no-daemon clean jar
  if errorlevel 1 (
    echo Build failed. See Gradle output above.
    pause
    endlocal
    exit /b 1
  )
)

rem find newest jar in build\libs (prefer shadow/fat jar)
set "JAR="
for /f "delims=" %%F in ('dir /b /a-d /o:-d "%~dp0build\libs\*.jar" 2^>nul') do (
  set "JAR=%~dp0build\libs\%%F"
  goto :found
)
echo No jar found in build\libs
pause
endlocal
exit /b 1

:found
echo Found jar: "%JAR%"
if not exist "%~dp0output" mkdir "%~dp0output"
copy /y "%JAR%" "%~dp0output\TitanCasino.jar" >nul
if errorlevel 1 (
  echo Failed to copy jar to output\TitanCasino.jar
  pause
  endlocal
  exit /b 1
)
echo JAR copied to output\TitanCasino.jar
echo Build successful.
pause
endlocal
exit /b 0