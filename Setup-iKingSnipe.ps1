# iKingSnipe Elite Casino v12.0 - Automated Setup Script
# This script verifies Java environments and configures the system for DreamBot & GoatGang GUI.

$ErrorActionPreference = "Stop"
$VERSION = "12.0"

Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  iKingSnipe Elite Casino v$VERSION Setup" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan

function Check-JavaVersion {
    param([int]$RequiredVersion)
    $javaPath = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaPath) {
        $versionInfo = & java -version 2>&1 | Out-String
        if ($versionInfo -match "version `"(1\.$RequiredVersion|$RequiredVersion)") {
            return $true
        }
    }
    return $false
}

# 1. Verify Java 8 (Required for DreamBot)
Write-Host "[1/4] Verifying Java 8 (DreamBot Compliance)..." -NoNewline
if (Check-JavaVersion 8) {
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host " ! Please install Java 8 (JRE/JDK) for DreamBot compatibility." -ForegroundColor Yellow
}

# 2. Verify Java 11+ (Required for GUI & Discord Bot)
Write-Host "[2/4] Verifying Java 11+ (GUI & Discord Bot)..." -NoNewline
if (Check-JavaVersion 11 -or Check-JavaVersion 17 -or Check-JavaVersion 21) {
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host " ! Please install Java 11 or higher for the Extensive Control Panel." -ForegroundColor Yellow
}

# 3. Configure Environment Variables
Write-Host "[3/4] Configuring Environment Variables..." -NoNewline
try {
    $currentPath = Get-Location
    [Environment]::SetEnvironmentVariable("IKINGSNIPE_HOME", $currentPath, "User")
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " FAILED" -ForegroundColor Red
}

# 4. Deploy to DreamBot
Write-Host "[4/4] Deploying JAR to DreamBot Scripts..." -NoNewline
$dbScripts = "$env:USERPROFILE\DreamBot\Scripts"
if (Test-Path $dbScripts) {
    Copy-Item ".\iKingSnipe-Complete-v12.jar" -Destination "$dbScripts\iKingSnipe-Complete-v12.jar" -Force
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " SKIPPED (DreamBot folder not found)" -ForegroundColor Yellow
}

Write-Host "`n═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host " Setup Complete! You can now run start.bat" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
pause
