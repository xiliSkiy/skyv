# SkyEye Docker Environment Startup Script (Windows PowerShell)

# Try setting output encoding (these might not work in all environments)
try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
    chcp 65001 | Out-Null
} catch {
    # Ignore encoding errors and continue with script
}

# Using ASCII characters instead of Chinese to avoid encoding issues
Write-Host "=== SkyEye Docker Environment Startup Script ===" -ForegroundColor Cyan
Write-Host "Preparing environment..." -ForegroundColor Green

# Create necessary directories
Write-Host "Creating directory structure..." -ForegroundColor Green
New-Item -Path "docker/mysql/data" -ItemType Directory -Force | Out-Null
New-Item -Path "docker/mysql/conf" -ItemType Directory -Force | Out-Null
New-Item -Path "docker/mysql/init" -ItemType Directory -Force | Out-Null
New-Item -Path "docker/redis/data" -ItemType Directory -Force | Out-Null
New-Item -Path "docker/redis/conf" -ItemType Directory -Force | Out-Null

# Copy configuration files
Write-Host "Copying configuration files..." -ForegroundColor Green
if (-not (Test-Path "docker/mysql/conf/my.cnf")) {
    Copy-Item -Path "docker/mysql/conf/my.cnf.template" -Destination "docker/mysql/conf/my.cnf" -ErrorAction SilentlyContinue
    if (-not $?) {
        Write-Host "Warning: MySQL config template not found" -ForegroundColor Yellow
    }
} else {
    Write-Host "MySQL config file already exists" -ForegroundColor Yellow
}

if (-not (Test-Path "docker/redis/conf/redis.conf")) {
    Copy-Item -Path "docker/redis/conf/redis.conf.template" -Destination "docker/redis/conf/redis.conf" -ErrorAction SilentlyContinue
    if (-not $?) {
        Write-Host "Warning: Redis config template not found" -ForegroundColor Yellow
    }
} else {
    Write-Host "Redis config file already exists" -ForegroundColor Yellow
}

# Copy initialization SQL script
Write-Host "Copying SQL initialization script..." -ForegroundColor Green
if (-not (Test-Path "docker/mysql/init/init.sql")) {
    Copy-Item -Path "docker/mysql/init/init.sql.template" -Destination "docker/mysql/init/init.sql" -ErrorAction SilentlyContinue
    if (-not $?) {
        Write-Host "Warning: SQL init template not found" -ForegroundColor Yellow
    }
} else {
    Write-Host "SQL init script already exists" -ForegroundColor Yellow
}

# Check Docker availability using multiple methods for Windows 11 compatibility
Write-Host "Checking Docker availability..." -ForegroundColor Green
$dockerRunning = $false

# Method 1: Try running a simple docker command
try {
    $dockerVersion = docker version --format '{{.Server.Version}}' 2>$null
    if ($dockerVersion) {
        $dockerRunning = $true
        Write-Host "Docker is running (Version: $dockerVersion)" -ForegroundColor Green
    }
} catch {
    # Docker command failed
}

# Method 2: Check Docker service if Method 1 failed
if (-not $dockerRunning) {
    try {
        # Check if Docker Desktop is running as a process
        $dockerDesktop = Get-Process 'Docker Desktop' -ErrorAction SilentlyContinue
        if ($dockerDesktop) {
            $dockerRunning = $true
            Write-Host "Docker Desktop process is running" -ForegroundColor Green
        } else {
            # Try using the service name. Docker might be running as 'com.docker.service' in some setups
            $dockerService = Get-Service -Name "com.docker.service" -ErrorAction SilentlyContinue
            if ($null -ne $dockerService -and $dockerService.Status -eq "Running") {
                $dockerRunning = $true
                Write-Host "Docker service is running" -ForegroundColor Green
            }
        }
    } catch {
        # Service check failed
    }
}

# Final Docker check with explicit message
if (-not $dockerRunning) {
    Write-Host "WARNING: Docker might not be running properly." -ForegroundColor Yellow
    Write-Host "Do you want to continue anyway? This might fail if Docker is not ready." -ForegroundColor Yellow
    $continue = Read-Host "Continue? (Y/N)"
    if ($continue -ne "Y" -and $continue -ne "y") {
        Write-Host "Aborting. Please start Docker Desktop and try again." -ForegroundColor Red
        exit 1
    }
    Write-Host "Continuing despite Docker check failure..." -ForegroundColor Yellow
}

# Check for proxy settings that might cause issues
Write-Host "Checking for proxy configuration issues..." -ForegroundColor Yellow
$proxyEnvVars = @("HTTP_PROXY", "HTTPS_PROXY", "NO_PROXY", "http_proxy", "https_proxy", "no_proxy")
$foundProxySettings = $false

foreach ($var in $proxyEnvVars) {
    if ([System.Environment]::GetEnvironmentVariable($var, "User") -or 
        [System.Environment]::GetEnvironmentVariable($var, "Machine")) {
        $foundProxySettings = $true
        Write-Host "Found proxy setting: $var" -ForegroundColor Yellow
    }
}

# Check .docker/config.json for proxy settings
$dockerConfig = "$env:USERPROFILE\.docker\config.json"
if (Test-Path $dockerConfig) {
    $configContent = Get-Content $dockerConfig -Raw | ConvertFrom-Json -ErrorAction SilentlyContinue
    if ($configContent.proxies.default.httpProxy -or $configContent.proxies.default.httpsProxy) {
        $foundProxySettings = $true
        Write-Host "Found proxy settings in Docker config file" -ForegroundColor Yellow
    }
}

if ($foundProxySettings) {
    Write-Host "Proxy settings detected which might cause connection issues." -ForegroundColor Yellow
    Write-Host "Would you like to use China mirrors for Docker images?" -ForegroundColor Cyan
    $useMirror = Read-Host "Use China mirrors? (Y/N)"
    
    if ($useMirror -eq "Y" -or $useMirror -eq "y") {
        # Use China mirrors in docker-compose command
        Write-Host "Using China mirrors for Docker images..." -ForegroundColor Green
        
        # Create or update .env file with mirror settings
        @"
# Docker image mirrors for China
MYSQL_IMAGE=registry.cn-hangzhou.aliyuncs.com/skyeye/mysql:5.7
REDIS_IMAGE=registry.cn-hangzhou.aliyuncs.com/skyeye/redis:6.2
"@ | Out-File -FilePath ".env" -Encoding utf8
        
        Write-Host "Created .env file with China mirror settings" -ForegroundColor Green
        
        # Check if docker-compose.yml exists and use .env
        if (-not (Test-Path "docker-compose.yml")) {
            # Create basic docker-compose.yml if it doesn't exist
            @"
version: '3'
services:
  mysql:
    image: \${MYSQL_IMAGE}
    container_name: skyeye-mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=skyeye123
      - MYSQL_DATABASE=skyeye
      - MYSQL_USER=skyeye
      - MYSQL_PASSWORD=skyeye123
    volumes:
      - ./docker/mysql/data:/var/lib/mysql
      - ./docker/mysql/conf/my.cnf:/etc/mysql/my.cnf
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    restart: always

  redis:
    image: \${REDIS_IMAGE}
    container_name: skyeye-redis
    ports:
      - "6379:6379"
    volumes:
      - ./docker/redis/data:/data
      - ./docker/redis/conf/redis.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
    restart: always
"@ | Out-File -FilePath "docker-compose.yml" -Encoding utf8
            Write-Host "Created docker-compose.yml file with mirror settings" -ForegroundColor Green
        } else {
            Write-Host "Using existing docker-compose.yml with .env file for mirrors" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Continuing with default Docker Hub registry..." -ForegroundColor Yellow
        Write-Host "If you encounter connection issues, run fix-docker-proxy.ps1 as Administrator" -ForegroundColor Yellow
    }
}

# Start Docker containers
Write-Host "Starting Docker containers..." -ForegroundColor Green
docker-compose up -d

# Check container status
Write-Host "Checking container status..." -ForegroundColor Green
Start-Sleep -Seconds 5
docker-compose ps

Write-Host "=== Environment startup completed ===" -ForegroundColor Cyan
Write-Host "MySQL: localhost:3306 (username: skyeye, password: skyeye123)" -ForegroundColor Green
Write-Host "Redis: localhost:6379" -ForegroundColor Green
Write-Host "Database name: skyeye" -ForegroundColor Green
Write-Host ""
Write-Host "Note: MySQL might take some time to initialize on first startup, please be patient." -ForegroundColor Yellow
Write-Host "      You can use 'docker-compose logs mysql' to check MySQL startup logs." -ForegroundColor Yellow

# If there were errors, show additional help
if ($LASTEXITCODE -ne 0) {
    Write-Host "`nThere were errors during startup. You may need to:" -ForegroundColor Red
    Write-Host "1. Run fix-docker-proxy.ps1 as Administrator to fix proxy issues" -ForegroundColor Yellow
    Write-Host "2. Check your Docker installation and network connection" -ForegroundColor Yellow
    Write-Host "3. Manually download the required images using:" -ForegroundColor Yellow
    Write-Host "   docker pull mysql:5.7" -ForegroundColor Yellow
    Write-Host "   docker pull redis:6.2" -ForegroundColor Yellow
} 