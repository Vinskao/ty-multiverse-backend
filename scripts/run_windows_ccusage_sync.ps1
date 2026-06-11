$ErrorActionPreference = "Stop"

$projectDir = Resolve-Path "$PSScriptRoot\.."
Set-Location -LiteralPath $projectDir.Path

$env:TYMB_URL = [Environment]::GetEnvironmentVariable("TYMB_URL", "User")
$env:AI_USAGE_INGEST_TOKEN = [Environment]::GetEnvironmentVariable("AI_USAGE_INGEST_TOKEN", "User")
$env:AI_USAGE_SOURCE_DEVICE = [Environment]::GetEnvironmentVariable("AI_USAGE_SOURCE_DEVICE", "User")

if (-not $env:TYMB_URL) {
    $env:TYMB_URL = "https://peoplesystem.tatdvsonorth.com/tymb"
}

if (-not $env:AI_USAGE_SOURCE_DEVICE) {
    $env:AI_USAGE_SOURCE_DEVICE = $env:COMPUTERNAME
}

python "$PSScriptRoot\sync_ccusage_direct_db.py"
