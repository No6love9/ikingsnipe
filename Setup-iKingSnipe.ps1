# iKingSnipe Global Solution - Ultimate Setup Script
# This script ensures your environment is 100% ready for the GoatGang Casino.

$ErrorActionPreference = "Stop"
$VERSION = "12.0"

Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  iKingSnipe Global Solution v$VERSION Setup" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan

# 1. Check for Java 8 (DreamBot)
Write-Host "[1/4] Checking Java 8 (DreamBot Compatibility)..." -NoNewline
$java8 = Get-ChildItem -Path "C:\Program Files\Java", "C:\Program Files (x86)\Java" -Recurse -Filter "java.exe" -ErrorAction SilentlyContinue | Where-Object { $_.VersionInfo.ProductVersion -like "1.8*" } | Select-Object -First 1
if ($java8) {
    Write-Host " FOUND: $($java8.FullName)" -ForegroundColor Green
} else {
    Write-Host " NOT FOUND!" -ForegroundColor Red
    Write-Host " Please install Java 8 (JRE) from https://www.java.com/" -ForegroundColor Yellow
}

# 2. Check for Java 11+ (GUI/Discord)
Write-Host "[2/4] Checking Java 11+ (GUI/Discord Compatibility)..." -NoNewline
$java11 = Get-Command java -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
if ($java11 -and (java -version 2>&1 | Out-String) -match 'version "(11|17|21)') {
    Write-Host " FOUND: $java11" -ForegroundColor Green
} else {
    Write-Host " NOT FOUND!" -ForegroundColor Red
    Write-Host " Please install Java 11 or 17 from https://adoptium.net/" -ForegroundColor Yellow
}

# 3. Create DreamBot Scripts Folder
Write-Host "[3/4] Preparing DreamBot Folders..." -NoNewline
$dbPath = "$env:USERPROFILE\DreamBot\Scripts"
if (!(Test-Path $dbPath)) {
    New-Item -Path $dbPath -ItemType Directory -Force | Out-Null
}
Write-Host " DONE" -ForegroundColor Green

# 4. Deploy JAR
Write-Host "[4/4] Deploying iKingSnipe-Complete-v12.jar..." -NoNewline
if (Test-Path "iKingSnipe-Complete-v12.jar") {
    Copy-Item "iKingSnipe-Complete-v12.jar" -Destination "$dbPath\iKingSnipe-Complete-v12.jar" -Force
    Write-Host " SUCCESS" -ForegroundColor Green
} else {
    Write-Host " JAR NOT FOUND IN CURRENT DIRECTORY!" -ForegroundColor Red
}

Write-Host "`n═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  SETUP COMPLETE! YOU ARE READY TO JOIN THE GOATGANG." -ForegroundColor Green
Write-Host "  1. Run 'start.bat' to configure your bot." -ForegroundColor White
Write-Host "  2. Open DreamBot and start 'iKingSnipe Global Casino'." -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Pause
