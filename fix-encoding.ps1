# Fix Java Compilation Encoding Issues for Windows 11

# Set console code page to UTF-8
Write-Host "=== Java Compilation Encoding Fix Script ===" -ForegroundColor Cyan
Write-Host "Setting console to UTF-8 mode..." -ForegroundColor Green

# Try to set console code page to UTF-8 (65001)
chcp 65001

# Create or modify Maven settings to use UTF-8
$mavenDir = "$env:USERPROFILE\.m2"
$settingsFile = "$mavenDir\settings.xml"

# Create .m2 directory if it doesn't exist
if (-not (Test-Path $mavenDir)) {
    New-Item -Path $mavenDir -ItemType Directory -Force | Out-Null
    Write-Host "Created Maven directory: $mavenDir" -ForegroundColor Green
}

# Check if settings file exists
$createNew = $true
if (Test-Path $settingsFile) {
    Write-Host "Found existing Maven settings file." -ForegroundColor Yellow
    Write-Host "Creating backup before modification..." -ForegroundColor Yellow
    Copy-Item -Path $settingsFile -Destination "$settingsFile.bak" -Force
    
    # Check if file already has encoding settings
    $content = Get-Content -Path $settingsFile -Raw
    if ($content -match "<encoding>UTF-8</encoding>") {
        Write-Host "Maven settings already contain UTF-8 encoding configuration." -ForegroundColor Green
        $createNew = $false
    }
}

if ($createNew) {
    # Create or update Maven settings.xml
    $settingsContent = @"
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <profiles>
    <profile>
      <id>encoding-profile</id>
      <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <encoding>UTF-8</encoding>
        <file.encoding>UTF-8</file.encoding>
      </properties>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>encoding-profile</activeProfile>
  </activeProfiles>
  
</settings>
"@
    
    Set-Content -Path $settingsFile -Value $settingsContent -Encoding UTF8
    Write-Host "Created/Updated Maven settings with UTF-8 encoding configuration." -ForegroundColor Green
}

# Create a batch file wrapper for mvn command with correct encoding
$mvnWrapperContent = @"
@echo off
rem UTF-8 Encoding Wrapper for Maven
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
mvn %*
"@

$mvnWrapperFile = "mvn-utf8.cmd"
Set-Content -Path $mvnWrapperFile -Value $mvnWrapperContent -Encoding ASCII
Write-Host "Created Maven UTF-8 wrapper script: $mvnWrapperFile" -ForegroundColor Green

# Create JAVA_TOOL_OPTIONS environment variable
Write-Host "`nSetting JAVA_TOOL_OPTIONS environment variable..." -ForegroundColor Yellow
[Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8", "User")
Write-Host "Environment variable set for current user." -ForegroundColor Green

# Modify project's pom.xml if it exists
$pomFile = "pom.xml"
if (Test-Path $pomFile) {
    Write-Host "`nUpdating project's pom.xml file..." -ForegroundColor Yellow
    
    # Create backup
    Copy-Item -Path $pomFile -Destination "$pomFile.bak" -Force
    Write-Host "Created backup: $pomFile.bak" -ForegroundColor Green
    
    # Read pom.xml content
    $pomContent = Get-Content -Path $pomFile -Raw
    
    # Check if properties section exists
    if ($pomContent -match "<properties>.*?</properties>") {
        # Check if encoding properties exist
        $hasSourceEncoding = $pomContent -match "<project.build.sourceEncoding>"
        $hasOutputEncoding = $pomContent -match "<project.reporting.outputEncoding>"
        
        if (-not ($hasSourceEncoding -and $hasOutputEncoding)) {
            # Add encoding properties to existing properties section
            $pomContent = $pomContent -replace "(<properties>)(.*?)(</properties>)", '$1$2    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
$3'
            Set-Content -Path $pomFile -Value $pomContent -Encoding UTF8
            Write-Host "Added encoding properties to pom.xml" -ForegroundColor Green
        } else {
            Write-Host "Encoding properties already exist in pom.xml" -ForegroundColor Green
        }
    } else {
        # Add properties section with encoding properties
        $pomContent = $pomContent -replace "(<project>.*?>)", '$1
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>'
        Set-Content -Path $pomFile -Value $pomContent -Encoding UTF8
        Write-Host "Added properties section with encoding settings to pom.xml" -ForegroundColor Green
    }
}

Write-Host "`n=== Instructions ===" -ForegroundColor Cyan
Write-Host "1. Restart your command prompt or PowerShell" -ForegroundColor White
Write-Host "2. For compilations, use the wrapper script: ./mvn-utf8.cmd compile" -ForegroundColor White
Write-Host "   (Instead of: mvn compile)" -ForegroundColor White
Write-Host "3. If using an IDE (Eclipse, IntelliJ), restart it for changes to take effect" -ForegroundColor White
Write-Host "`nAdditional tips:" -ForegroundColor Yellow
Write-Host "- Edit -> Run Configuration in your IDE to add -Dfile.encoding=UTF-8 to VM options" -ForegroundColor White
Write-Host "- In IntelliJ IDEA, go to File -> Settings -> Editor -> File Encodings and set all to UTF-8" -ForegroundColor White
Write-Host "- In VS Code, add \"java.jdt.ls.vmargs\": \"-Dfile.encoding=UTF-8\" to settings.json" -ForegroundColor White
Write-Host "`nFor Windows Terminal, consider setting UTF-8 as default in settings.json:" -ForegroundColor White
Write-Host "  \"profiles\": { \"defaults\": { \"font\": { \"face\": \"Consolas\" }, \"codePage\": 65001 } }" -ForegroundColor White 