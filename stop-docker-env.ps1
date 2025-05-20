# SkyEye Docker Environment Stop Script (Windows PowerShell)

# Try setting output encoding (these might not work in all environments)
try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
} catch {
    # Ignore encoding errors and continue with script
}

Write-Host "=== SkyEye Docker Environment Stop Script ===" -ForegroundColor Cyan
Write-Host "Stopping Docker containers..." -ForegroundColor Yellow

# Check if docker-compose is available
try {
    $null = docker-compose --version
} catch {
    Write-Host "Error: docker-compose command not found." -ForegroundColor Red
    Write-Host "Please make sure Docker Desktop is installed and in your PATH." -ForegroundColor Red
    exit 1
}

# Stop Docker containers
docker-compose down

# Check if the stop was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "=== Environment stopped successfully ===" -ForegroundColor Green
} else {
    Write-Host "=== Warning: There were issues stopping the environment ===" -ForegroundColor Red
    Write-Host "You may need to stop containers manually using Docker Desktop." -ForegroundColor Yellow
}

# Optional: Show current running containers to verify
Write-Host "`nVerifying no containers are running for this project:" -ForegroundColor Cyan
docker-compose ps 