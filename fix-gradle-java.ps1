# fix-gradle-java.ps1
$ErrorActionPreference = "Stop"

Write-Host "=== Elite Titan Casino - Gradle Java Fix ===`n"

# 1. Detect JDK 21 under Eclipse Adoptium
$jdk21 = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like "jdk-21*" } |
    Select-Object -First 1

if (-not $jdk21) {
    Write-Host "[!] No JDK 21 found under C:\Program Files\Eclipse Adoptium\" -ForegroundColor Red
    Write-Host "    Install Temurin JDK 21 (LTS) from: https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "    Then re-run this script."
    exit 1
}

$jdkPath = $jdk21.FullName
Write-Host "[+] Detected JDK 21 at: $jdkPath" -ForegroundColor Green

# 2. Ensure we're in the project root
$projectRoot = "C:\Users\no6lo\GitHub\ikingsnipe"
Set-Location $projectRoot
Write-Host "[+] Project root: $projectRoot"

# 3. Write/patch gradle.properties
$gradlePropsPath = Join-Path $projectRoot "gradle.properties"
$javaHomeLine   = "org.gradle.java.home=$($jdkPath -replace '\\','/')"

if (Test-Path $gradlePropsPath) {
    $content = Get-Content $gradlePropsPath
    $existing = $content | Where-Object { $_ -match '^org\.gradle\.java\.home=' }

    if ($existing) {
        $newContent = $content -replace '^org\.gradle\.java\.home=.*', $javaHomeLine
    } else {
        $newContent = $content + $javaHomeLine
    }

    $newContent | Set-Content $gradlePropsPath -Encoding UTF8
    Write-Host "[+] Updated existing gradle.properties with:" -ForegroundColor Green
    Write-Host "    $javaHomeLine"
} else {
    $javaHomeLine | Set-Content $gradlePropsPath -Encoding UTF8
    Write-Host "[+] Created gradle.properties with:" -ForegroundColor Green
    Write-Host "    $javaHomeLine"
}

# 4. Verify Gradle JVM
Write-Host "`n[+] Verifying Gradle JVM..." -ForegroundColor Cyan
& .\gradlew.bat --version

Write-Host "`n[+] Running build: clean shadowJar --no-daemon`n" -ForegroundColor Cyan
& .\gradlew.bat clean shadowJar --no-daemon
