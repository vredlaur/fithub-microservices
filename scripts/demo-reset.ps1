param(
    [switch]$SkipCheck
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot

Set-Location $repoRoot

Write-Host "Resetting FitHub Docker demo environment..." -ForegroundColor Yellow
docker compose down -v
docker compose up -d --build

if (-not $SkipCheck) {
    Write-Host ""
    Write-Host "Waiting for services and running demo verification..." -ForegroundColor Yellow
    & "$PSScriptRoot\demo-check.ps1"
}

Write-Host ""
Write-Host "Demo environment is ready." -ForegroundColor Green
