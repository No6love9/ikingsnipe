#Requires -Version 5.0
<#
.SYNOPSIS
    Robust Java Installer for iKingSnipe DreamBot Script
    Installs Java 8 (for DreamBot) and Java 11 (for script compilation)
    
.DESCRIPTION
    This script automates Java installation with:
    - Automatic JDK 8 and JDK 11 detection and installation
    - Path validation and error recovery
    - Environment variable configuration
    - JAVA_HOME setup for both versions
    - Gradle wrapper compatibility
    - DreamBot integration support
    
.PARAMETER JavaVersion
    Specific Java version to install: '8', '11', or 'both' (default: 'both')
    
.PARAMETER Force
    Force reinstall even if Java is already installed
    
.PARAMETER Silent
    Run without prompts (non-interactive mode)
    
.EXAMPLE
    .\Install-JavaDependencies.ps1
    .\Install-JavaDependencies.ps1 -JavaVersion 11 -Force
    .\Install-JavaDependencies.ps1 -Silent
#>

param(
    [ValidateSet('8', '11', 'both')]
    [string]$JavaVersion = 'both',
    
    [switch]$Force,
    [switch]$Silent
)

# ============================================================================
# CONFIGURATION
# ============================================================================

$ErrorActionPreference = 'Stop'
$WarningPreference = 'Continue'

# Script metadata
$SCRIPT_VERSION = "2026.1.0"
$SCRIPT_NAME = "iKingSnipe Java Installer"

# Java versions to install
$JAVA_8_VERSION = "8.0.402"
$JAVA_11_VERSION = "11.0.21"

# Installation paths
$INSTALL_BASE = "C:\Program Files\Java"
$JAVA8_HOME = "$INSTALL_BASE\jdk-8"
$JAVA11_HOME = "$INSTALL_BASE\jdk-11"
$DREAMBOT_JAVA = $JAVA8_HOME  # DreamBot uses Java 8

# Download URLs (using official OpenJDK/Eclipse Temurin)
$JAVA8_URL = "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u402-b06/OpenJDK8U-jdk_x64_windows_hotspot_8u402b06.zip"
$JAVA11_URL = "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.21%2B9/OpenJDK11U-jdk_x64_windows_hotspot_11.0.21_9.zip"

# Temp directory
$TEMP_DIR = "$env:TEMP\JavaInstaller_$(Get-Random)"

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

function Test-JavaInstalled {
    param([string]$Version)
    
    try {
        $javaPath = if ($Version -eq "8") { "$JAVA8_HOME\bin\java.exe" } else { "$JAVA11_HOME\bin\java.exe" }
        
        if (Test-Path $javaPath) {
            $output = & $javaPath -version 2>&1
            Write-Status "Java $Version found at: $javaPath" "SUCCESS"
            return $true
        }
        return $false
    }
    catch {
        return $false
    }
}

function Get-JavaVersion {
    param([string]$JavaPath)
    
    try {
        $output = & $JavaPath -version 2>&1
        if ($output -match 'version "([^"]+)"') {
            return $matches[1]
        }
        return "Unknown"
    }
    catch {
        return "Error"
    }
}

function Test-PathValid {
    param([string]$Path)
    
    try {
        if (-not (Test-Path $Path)) {
            New-Item -ItemType Directory -Path $Path -Force | Out-Null
            Write-Status "Created directory: $Path" "INFO"
        }
        return $true
    }
    catch {
        Write-Status "Failed to create/access path: $Path - $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Download-File {
    param(
        [string]$Url,
        [string]$OutputPath,
        [string]$Description
    )
    
    Write-Status "Downloading $Description..." "INFO"
    
    try {
        # Use TLS 1.2 for secure downloads
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $Url -OutFile $OutputPath -UseBasicParsing -TimeoutSec 300
        $ProgressPreference = 'Continue'
        
        if (Test-Path $OutputPath) {
            $size = (Get-Item $OutputPath).Length / 1MB
            Write-Status "Downloaded successfully ($([math]::Round($size, 2)) MB)" "SUCCESS"
            return $true
        }
        else {
            Write-Status "Download verification failed" "ERROR"
            return $false
        }
    }
    catch {
        Write-Status "Download failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Extract-Archive {
    param(
        [string]$ZipPath,
        [string]$ExtractPath,
        [string]$Description
    )
    
    Write-Status "Extracting $Description..." "INFO"
    
    try {
        if (-not (Test-Path $ExtractPath)) {
            New-Item -ItemType Directory -Path $ExtractPath -Force | Out-Null
        }
        
        # Use built-in Expand-Archive (PowerShell 5.0+)
        Expand-Archive -Path $ZipPath -DestinationPath $ExtractPath -Force
        
        Write-Status "Extraction completed" "SUCCESS"
        return $true
    }
    catch {
        Write-Status "Extraction failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Set-EnvironmentVariable {
    param(
        [string]$Name,
        [string]$Value,
        [ValidateSet('User', 'Machine')]
        [string]$Scope = 'User'
    )
    
    try {
        [Environment]::SetEnvironmentVariable($Name, $Value, $Scope)
        Write-Status "Set $Name = $Value ($Scope)" "SUCCESS"
        return $true
    }
    catch {
        Write-Status "Failed to set environment variable $Name : $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Update-PathVariable {
    param([string]$NewPath)
    
    try {
        $currentPath = [Environment]::GetEnvironmentVariable('PATH', 'User')
        
        if ($currentPath -notlike "*$NewPath*") {
            $newPathValue = "$NewPath;$currentPath"
            [Environment]::SetEnvironmentVariable('PATH', $newPathValue, 'User')
            Write-Status "Added to PATH: $NewPath" "SUCCESS"
            return $true
        }
        else {
            Write-Status "Path already in PATH: $NewPath" "INFO"
            return $true
        }
    }
    catch {
        Write-Status "Failed to update PATH: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Install-JavaVersion {
    param(
        [string]$Version,
        [string]$Url,
        [string]$TargetPath,
        [string]$EnvironmentVar
    )
    
    Write-Header "Installing Java $Version"
    
    # Check if already installed
    if ((Test-JavaInstalled -Version $Version) -and -not $Force) {
        Write-Status "Java $Version is already installed" "SUCCESS"
        return $true
    }
    
    if ($Force -and (Test-Path $TargetPath)) {
        Write-Status "Removing existing Java $Version installation..." "INFO"
        Remove-Item -Path $TargetPath -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # Create temp directory
    if (-not (Test-Path $TEMP_DIR)) {
        New-Item -ItemType Directory -Path $TEMP_DIR -Force | Out-Null
    }
    
    $zipFile = "$TEMP_DIR\java$Version.zip"
    
    # Download
    if (-not (Download-File -Url $Url -OutputPath $zipFile -Description "Java $Version")) {
        return $false
    }
    
    # Extract
    if (-not (Extract-Archive -ZipPath $zipFile -ExtractPath $TargetPath -Description "Java $Version")) {
        return $false
    }
    
    # Clean up zip
    Remove-Item -Path $zipFile -Force -ErrorAction SilentlyContinue
    
    # Set environment variables
    if (-not (Set-EnvironmentVariable -Name $EnvironmentVar -Value $TargetPath -Scope User)) {
        Write-Status "Warning: Could not set environment variable, but installation may still work" "WARNING"
    }
    
    # Add to PATH
    if (-not (Update-PathVariable -NewPath "$TargetPath\bin")) {
        Write-Status "Warning: Could not update PATH, but installation may still work" "WARNING"
    }
    
    # Verify installation
    if (Test-JavaInstalled -Version $Version) {
        $javaPath = if ($Version -eq "8") { "$JAVA8_HOME\bin\java.exe" } else { "$JAVA11_HOME\bin\java.exe" }
        $detectedVersion = Get-JavaVersion -JavaPath $javaPath
        Write-Status "Java $Version installed successfully (Version: $detectedVersion)" "SUCCESS"
        return $true
    }
    else {
        Write-Status "Java $Version installation verification failed" "ERROR"
        return $false
    }
}

function Validate-Installation {
    Write-Header "Validating Installation"
    
    $allValid = $true
    
    # Check Java 8
    if (($JavaVersion -eq '8' -or $JavaVersion -eq 'both') -and (Test-JavaInstalled -Version "8")) {
        $java8Path = "$JAVA8_HOME\bin\java.exe"
        $java8Version = Get-JavaVersion -JavaPath $java8Path
        Write-Status "✓ Java 8: $java8Version" "SUCCESS"
    }
    else {
        Write-Status "✗ Java 8: Not found or not requested" "WARNING"
        if ($JavaVersion -eq '8' -or $JavaVersion -eq 'both') { $allValid = $false }
    }
    
    # Check Java 11
    if (($JavaVersion -eq '11' -or $JavaVersion -eq 'both') -and (Test-JavaInstalled -Version "11")) {
        $java11Path = "$JAVA11_HOME\bin\java.exe"
        $java11Version = Get-JavaVersion -JavaPath $java11Path
        Write-Status "✓ Java 11: $java11Version" "SUCCESS"
    }
    else {
        Write-Status "✗ Java 11: Not found or not requested" "WARNING"
        if ($JavaVersion -eq '11' -or $JavaVersion -eq 'both') { $allValid = $false }
    }
    
    # Check environment variables
    Write-Status "JAVA8_HOME: $([Environment]::GetEnvironmentVariable('JAVA8_HOME', 'User'))" "INFO"
    Write-Status "JAVA11_HOME: $([Environment]::GetEnvironmentVariable('JAVA11_HOME', 'User'))" "INFO"
    Write-Status "JAVA_HOME: $([Environment]::GetEnvironmentVariable('JAVA_HOME', 'User'))" "INFO"
    
    return $allValid
}

function Show-Instructions {
    Write-Header "Next Steps"
    
    Write-Host @"
1. RESTART YOUR TERMINAL/IDE
   - Close and reopen PowerShell, CMD, or your IDE
   - This ensures environment variables are reloaded
   
2. VERIFY INSTALLATION
   java -version
   javac -version
   
3. BUILD THE PROJECT
   cd ikingsnipe
   .\gradlew clean build
   
4. DREAMBOT INTEGRATION
   - Copy compiled JAR to: C:\Users\YourName\DreamBot\Scripts\
   - Or use the auto-placement feature (see below)
   
5. AUTO-PLACEMENT (Optional)
   Run: .\Deploy-ToD reambotFolder.ps1
   This automatically copies the JAR to your DreamBot scripts folder
   
6. TROUBLESHOOTING
   - If "java not found": Restart your terminal
   - If "gradle not found": Check JAVA_HOME is set correctly
   - Run: Get-Command java (should show path)
   
"@
}

function Cleanup {
    Write-Status "Cleaning up temporary files..." "INFO"
    if (Test-Path $TEMP_DIR) {
        Remove-Item -Path $TEMP_DIR -Recurse -Force -ErrorAction SilentlyContinue
        Write-Status "Cleanup completed" "SUCCESS"
    }
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

function Main {
    Write-Header "$SCRIPT_NAME v$SCRIPT_VERSION"
    
    Write-Status "Script Version: $SCRIPT_VERSION" "INFO"
    Write-Status "Target Java Versions: $JavaVersion" "INFO"
    Write-Status "Installation Base: $INSTALL_BASE" "INFO"
    Write-Status "Force Reinstall: $Force" "INFO"
    
    # Check admin privileges
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    if (-not $isAdmin) {
        Write-Status "This script requires Administrator privileges" "WARNING"
        Write-Status "Please run PowerShell as Administrator" "ERROR"
        exit 1
    }
    
    Write-Status "Administrator privileges confirmed" "SUCCESS"
    
    # Confirm before proceeding
    if (-not $Silent) {
        Write-Host ""
        Write-Host "This script will install Java $JavaVersion to: $INSTALL_BASE" -ForegroundColor Yellow
        Write-Host ""
        $response = Read-Host "Continue? (Y/n)"
        if ($response -eq 'n' -or $response -eq 'N') {
            Write-Status "Installation cancelled" "WARNING"
            exit 0
        }
    }
    
    $successCount = 0
    $failureCount = 0
    
    # Install Java 8
    if ($JavaVersion -eq '8' -or $JavaVersion -eq 'both') {
        if (Install-JavaVersion -Version "8" -Url $JAVA8_URL -TargetPath $JAVA8_HOME -EnvironmentVar "JAVA8_HOME") {
            $successCount++
        }
        else {
            $failureCount++
        }
    }
    
    # Install Java 11
    if ($JavaVersion -eq '11' -or $JavaVersion -eq 'both') {
        if (Install-JavaVersion -Version "11" -Url $JAVA11_URL -TargetPath $JAVA11_HOME -EnvironmentVar "JAVA11_HOME") {
            $successCount++
        }
        else {
            $failureCount++
        }
    }
    
    # Set default JAVA_HOME to Java 11 for compilation
    Set-EnvironmentVariable -Name "JAVA_HOME" -Value $JAVA11_HOME -Scope User
    
    # Validate
    $validationPassed = Validate-Installation
    
    # Cleanup
    Cleanup
    
    # Summary
    Write-Header "Installation Summary"
    Write-Status "Successful: $successCount" "SUCCESS"
    Write-Status "Failed: $failureCount" "ERROR"
    
    if ($validationPassed) {
        Write-Status "All validations passed!" "SUCCESS"
        Show-Instructions
        exit 0
    }
    else {
        Write-Status "Some validations failed. Please check the output above." "ERROR"
        exit 1
    }
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
