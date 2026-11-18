param(
    [string]$TaskName = "ccusage Auto Update",
    [string]$UpdateAt = "3:00AM"
)

$ErrorActionPreference = "Stop"

$updateScript = Join-Path $PSScriptRoot "update_windows_ccusage.ps1"
if (-not (Test-Path -LiteralPath $updateScript)) {
    throw "Update script not found: $updateScript"
}

$npmPath = (Get-Command npm.cmd -ErrorAction Stop).Source
$argument = "-NoProfile -NonInteractive -WindowStyle Hidden -ExecutionPolicy Bypass " +
    "-File `"$updateScript`" -NpmPath `"$npmPath`""
$action = New-ScheduledTaskAction `
    -Execute "powershell.exe" `
    -Argument $argument `
    -WorkingDirectory $PSScriptRoot
$trigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At $UpdateAt
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew
$principal = New-ScheduledTaskPrincipal `
    -UserId ([System.Security.Principal.WindowsIdentity]::GetCurrent().Name) `
    -LogonType Interactive `
    -RunLevel Limited

Register-ScheduledTask `
    -TaskName $TaskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Principal $principal `
    -Description "Update the current user's global ccusage npm package to the latest version weekly." `
    -Force | Out-Null

Write-Host "Installed scheduled task: $TaskName"
Write-Host "npm: $npmPath"
Write-Host "Schedule: Sundays at $UpdateAt (runs when available after sign-in)"
