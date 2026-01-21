#Requires -Version 5.0
<#
.SYNOPSIS
    Auto-Deploy iKingSnipe JAR to DreamBot Scripts Folder
    
.DESCRIPTION
    Automatically compiles the project and deploys the JAR to DreamBot's scripts folder.
    Handles path detection, error recovery, and validation.
    
.PARAMETER DreamBotPath
    Custom path to DreamBot installation. If not specified, script will search common locations.
    
.PARAMETER AutoBuild
    Automatically build the project before deployment (default: $true)
    
.PARAMETER BackupExisting
    Create backup of existing JAR before deploying (default: $true)
    
.PARAMETER Silent
    Run without prompts
    
.EXAMPLE
    .\Deploy-ToDreamBot.ps1
    .\Deploy-ToDreamBot.ps1 -DreamBotPath "C:\DreamBot"
    .\Deploy-ToDreamBot.ps1 -AutoBuild $false -Silent
#>

param(
    [string]$DreamBotPath,
    [bool]$AutoBuild = $true,
    [bool]$BackupExisting = $true,
    [switch]$Silent
)

$ErrorActionPreference = 'Stop'
$SCRIPT_VERSION = "2026.1.0"

# ============================================================================
# CONFIGURATION
# ============================================================================

$PROJECT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$JAR_NAME = "ikingsnipe-14.0.0-GOATGANG.jar"
$JAR_BUILD_PATH = "$PROJECT_ROOT\build\libs\$JAR_NAME"

# Common DreamBot installation paths
$COMMON_DREAMBOT_PATHS = @(
    "$env:USERPROFILE\DreamBot",
    "$env:USERPROFILE\AppData\Local\DreamBot",
    "$env:USERPROFILE\AppData\Roaming\DreamBot",
    "C:\DreamBot",
    "C:\Program Files\DreamBot",
    "C:\Program Files (x86)\DreamBot"
)

# ============================================================================
# UTILITY FUNCTIONS
# ============================================================================

function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║ $($Text.PadRight(62)) ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Status {
    param([string]$Message, [string]$Status = "INFO")
    $timestamp = Get-Date -Format "HH:mm:ss"
    $colors = @{
        "INFO"    = "Green"
        "WARNING" = "Yellow"
        "ERROR"   = "Red"
        "SUCCESS" = "Green"
    }
    $color = $colors[$Status] ?? "White"
    Write-Host "[$timestamp] [$Status] $Message" -ForegroundColor $color
}

function Find-DreamBotInstallation {
    Write-Status "Searching for DreamBot installation..." "INFO"
    
    foreach ($path in $COMMON_DREAMBOT_PATHS) {
        if (Test-Path $path) {
            $scriptsFolder = Join-Path $path "Scripts"
            if (Test-Path $scriptsFolder) {
                Write-Status "Found DreamBot at: $path" "SUCCESS"
                return $path
            }
        }
    }
    
    return $null
}

function Build-Project {
    Write-Header "Building Project"
    
    Write-Status "Checking for Gradle wrapper..." "INFO"
    $gradlewPath = "$PROJECT_ROOT\gradlew.bat"
    
    if (-not (Test-Path $gradlewPath)) {
        Write-Status "Gradle wrapper not found at: $gradlewPath" "ERROR"
        return $false
    }
    
    Write-Status "Starting build process..." "INFO"
    
    try {
        Push-Location $PROJECT_ROOT
        
        # Run gradle build
        & $gradlewPath clean build -x test 2>&1 | Tee-Object -Variable buildOutput
        
        Pop-Location
        
        if ($LASTEXITCODE -ne 0) {
            Write-Status "Build failed with exit code: $LASTEXITCODE" "ERROR"
            Write-Status "Build output:" "ERROR"
            Write-Host $buildOutput
            return $false
        }
        
        Write-Status "Build completed successfully" "SUCCESS"
        return $true
    }
    catch {
        Write-Status "Build failed: $($_.Exception.Message)" "ERROR"
        Pop-Location
        return $false
    }
}

function Verify-JAR {
    param([string]$JarPath)
    
    if (-not (Test-Path $JarPath)) {
        Write-Status "JAR not found at: $JarPath" "ERROR"
        return $false
    }
    
    $jarSize = (Get-Item $JarPath).Length / 1MB
    Write-Status "JAR verified: $JarPath ($([math]::Round($jarSize, 2)) MB)" "SUCCESS"
    return $true
}

function Backup-ExistingJAR {
    param(
        [string]$DestinationPath,
        [string]$JarName
    )
    
    $existingJar = Join-Path $DestinationPath $JarName
    
    if (Test-Path $existingJar) {
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $backupPath = "$existingJar.backup_$timestamp"
        
        try {
            Copy-Item -Path $existingJar -Destination $backupPath -Force
            Write-Status "Backup created: $backupPath" "SUCCESS"
            return $true
        }
        catch {
            Write-Status "Failed to create backup: $($_.Exception.Message)" "WARNING"
            return $false
        }
    }
    
    return $true
}

function Deploy-JAR {
    param(
        [string]$SourceJar,
        [string]$DestinationFolder
    )
    
    Write-Header "Deploying JAR"
    
    # Verify destination exists
    if (-not (Test-Path $DestinationFolder)) {
        Write-Status "Creating Scripts folder..." "INFO"
        try {
            New-Item -ItemType Directory -Path $DestinationFolder -Force | Out-Null
            Write-Status "Scripts folder created" "SUCCESS"
        }
        catch {
            Write-Status "Failed to create Scripts folder: $($_.Exception.Message)" "ERROR"
            return $false
        }
    }
    
    # Backup existing
    if ($BackupExisting) {
        Backup-ExistingJAR -DestinationPath $DestinationFolder -JarName $JAR_NAME
    }
    
    # Copy JAR
    try {
        $destinationPath = Join-Path $DestinationFolder $JAR_NAME
        Copy-Item -Path $SourceJar -Destination $destinationPath -Force
        Write-Status "JAR deployed successfully" "SUCCESS"
        Write-Status "Location: $destinationPath" "INFO"
        return $true
    }
    catch {
        Write-Status "Deployment failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Validate-Deployment {
    param(
        [string]$DreamBotPath,
        [string]$JarName
    )
    
    Write-Header "Validating Deployment"
    
    $jarPath = Join-Path $DreamBotPath "Scripts" $JarName
    
    if (Test-Path $jarPath) {
        $fileInfo = Get-Item $jarPath
        $size = $fileInfo.Length / 1MB
        $modified = $fileInfo.LastWriteTime
        
        Write-Status "✓ JAR found at: $jarPath" "SUCCESS"
        Write-Status "  Size: $([math]::Round($size, 2)) MB" "INFO"
        Write-Status "  Modified: $modified" "INFO"
        return $true
    }
    else {
        Write-Status "✗ JAR not found at expected location: $jarPath" "ERROR"
        return $false
    }
}

function Show-PostDeploymentInstructions {
    param([string]$DreamBotPath)
    
    Write-Header "Deployment Complete!"
    
    Write-Host @"
✓ JAR has been successfully deployed to DreamBot

NEXT STEPS:
1. Open DreamBot Client
2. Navigate to Scripts Manager
3. Search for "GoatGang" or "ikingsnipe"
4. Click "Load Script"
5. Configure settings in the GUI popup
6. Click "Start Script"

TROUBLESHOOTING:
- If script doesn't appear: Restart DreamBot client
- If script crashes: Check DreamBot logs for errors
- If JAR is outdated: Run this script again to redeploy

DREAMBOT PATH: $DreamBotPath
SCRIPTS FOLDER: $DreamBotPath\Scripts

"@
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

function Main {
    Write-Header "iKingSnipe DreamBot Deployer v$SCRIPT_VERSION"
    
    Write-Status "Project Root: $PROJECT_ROOT" "INFO"
    Write-Status "Auto-Build: $AutoBuild" "INFO"
    Write-Status "Backup Existing: $BackupExisting" "INFO"
    
    # Step 1: Build if requested
    if ($AutoBuild) {
        if (-not (Build-Project)) {
            Write-Status "Build failed. Aborting deployment." "ERROR"
            exit 1
        }
    }
    
    # Step 2: Verify JAR exists
    if (-not (Verify-JAR -JarPath $JAR_BUILD_PATH)) {
        Write-Status "JAR verification failed. Aborting deployment." "ERROR"
        exit 1
    }
    
    # Step 3: Find DreamBot installation
    if (-not $DreamBotPath) {
        $DreamBotPath = Find-DreamBotInstallation
        
        if (-not $DreamBotPath) {
            Write-Status "Could not find DreamBot installation" "ERROR"
            Write-Status "Please specify DreamBot path manually:" "INFO"
            Write-Host "  .\Deploy-ToDreamBot.ps1 -DreamBotPath 'C:\Path\To\DreamBot'" -ForegroundColor Yellow
            exit 1
        }
    }
    else {
        if (-not (Test-Path $DreamBotPath)) {
            Write-Status "Specified DreamBot path not found: $DreamBotPath" "ERROR"
            exit 1
        }
    }
    
    # Step 4: Confirm deployment
    if (-not $Silent) {
        Write-Host ""
        Write-Host "Ready to deploy to: $DreamBotPath\Scripts" -ForegroundColor Yellow
        Write-Host ""
        $response = Read-Host "Continue? (Y/n)"
        if ($response -eq 'n' -or $response -eq 'N') {
            Write-Status "Deployment cancelled" "WARNING"
            exit 0
        }
    }
    
    # Step 5: Deploy
    $scriptsFolder = Join-Path $DreamBotPath "Scripts"
    if (-not (Deploy-JAR -SourceJar $JAR_BUILD_PATH -DestinationFolder $scriptsFolder)) {
        Write-Status "Deployment failed" "ERROR"
        exit 1
    }
    
    # Step 6: Validate
    if (-not (Validate-Deployment -DreamBotPath $DreamBotPath -JarName $JAR_NAME)) {
        Write-Status "Validation failed" "ERROR"
        exit 1
    }
    
    # Step 7: Show instructions
    Show-PostDeploymentInstructions -DreamBotPath $DreamBotPath
    
    Write-Status "Deployment completed successfully!" "SUCCESS"
    exit 0
}

# Run main function
try {
    Main
}
catch {
    Write-Status "Fatal error: $($_.Exception.Message)" "ERROR"
    Write-Status "Stack trace: $($_.ScriptStackTrace)" "ERROR"
    exit 1
}
