param(
    [string]$TaskName = "TY Multiverse AI Usage Sync",
    [string]$ProjectDir = (Resolve-Path "$PSScriptRoot\..").Path,
    [string]$TymbUrl = "http://localhost:8080/tymb",
    [string]$SourceDevice = $env:COMPUTERNAME,
    [int]$IntervalMinutes = 180
)

$ErrorActionPreference = "Stop"

if (-not $env:AI_USAGE_INGEST_TOKEN) {
    throw "AI_USAGE_INGEST_TOKEN is required in the current user environment."
}

$scriptPath = Join-Path $ProjectDir "scripts\sync_ccusage_to_db.py"
if (-not (Test-Path -LiteralPath $scriptPath)) {
    throw "Sync script not found: $scriptPath"
}

$python = (Get-Command python -ErrorAction SilentlyContinue).Source
if (-not $python) {
    $python = (Get-Command python3 -ErrorAction SilentlyContinue).Source
}
if (-not $python) {
    throw "Python was not found in PATH."
}

$envBlock = @(
    "`$env:TYMB_URL = '$TymbUrl'",
    "`$env:AI_USAGE_INGEST_TOKEN = '$($env:AI_USAGE_INGEST_TOKEN)'",
    "`$env:AI_USAGE_SOURCE_DEVICE = '$SourceDevice'",
    "Set-Location -LiteralPath '$ProjectDir'",
    "& '$python' '$scriptPath'",
    "exit `$LASTEXITCODE"
) -join "; "

$argument = "-NoProfile -ExecutionPolicy Bypass -Command `"$envBlock`""
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument $argument
$logonTrigger = New-ScheduledTaskTrigger -AtLogOn
$intervalTrigger = New-ScheduledTaskTrigger -Once -At (Get-Date).AddMinutes(1) `
    -RepetitionInterval (New-TimeSpan -Minutes $IntervalMinutes)
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew

Register-ScheduledTask `
    -TaskName $TaskName `
    -Action $action `
    -Trigger @($logonTrigger, $intervalTrigger) `
    -Settings $settings `
    -Description "Sync ccusage daily token usage to TY Multiverse backend with a per-device source key." `
    -Force | Out-Null

Write-Host "Installed scheduled task: $TaskName"
Write-Host "ProjectDir: $ProjectDir"
Write-Host "TYMB_URL: $TymbUrl"
Write-Host "AI_USAGE_SOURCE_DEVICE: $SourceDevice"
Write-Host "Interval: every $IntervalMinutes minutes"
