param(
    [Parameter(Mandatory = $true)]
    [string]$NpmPath
)

$ErrorActionPreference = "Stop"

$logDirectory = Join-Path $env:LOCALAPPDATA "ccusage"
$logPath = Join-Path $logDirectory "auto-update.log"
$utf8NoBom = New-Object System.Text.UTF8Encoding -ArgumentList $false
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

function Add-LogLine {
    param([string]$Message)

    [System.IO.File]::AppendAllText(
        $logPath,
        "$Message$([Environment]::NewLine)",
        $utf8NoBom
    )
}

try {
    if (-not (Test-Path -LiteralPath $NpmPath)) {
        throw "npm executable not found: $NpmPath"
    }

    Add-LogLine "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Updating ccusage with $NpmPath"
    $npmOutput = & $NpmPath install --global "ccusage@latest" 2>&1
    $npmExitCode = $LASTEXITCODE
    foreach ($line in $npmOutput) {
        Add-LogLine $line.ToString()
    }
    if ($npmExitCode -ne 0) {
        throw "npm install failed with exit code $npmExitCode"
    }

    $ccusagePath = Join-Path (Split-Path -Parent $NpmPath) "ccusage.cmd"
    if (Test-Path -LiteralPath $ccusagePath) {
        $versionOutput = & $ccusagePath --version 2>&1
        $versionExitCode = $LASTEXITCODE
        foreach ($line in $versionOutput) {
            Add-LogLine $line.ToString()
        }
        if ($versionExitCode -ne 0) {
            throw "ccusage version check failed with exit code $versionExitCode"
        }
    }

    Add-LogLine "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Update completed"
}
catch {
    Add-LogLine "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Update failed: $_"
    throw
}
