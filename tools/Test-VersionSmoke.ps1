param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [int]$WarmupSeconds = 90,
    [int]$TimeoutSeconds = 300,
    [int]$PollSeconds = 3,
    [switch]$BuildFirst,
    [switch]$KeepClientOpen,
    [switch]$SkipServerCacheCheck,
    [Alias('ExtraArgs')]
    [string[]]$GradleArgs,
    [switch]$NoThrow
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ($WarmupSeconds -le 0) {
    throw 'WarmupSeconds must be greater than 0.'
}

if ($TimeoutSeconds -lt $WarmupSeconds) {
    throw 'TimeoutSeconds must be greater than or equal to WarmupSeconds.'
}

if ($PollSeconds -le 0) {
    throw 'PollSeconds must be greater than 0.'
}

function Get-CrashReportFiles {
    param(
        [string]$ProjectPath
    )

    $candidateDirs = @(
        (Join-Path $ProjectPath 'crash-reports'),
        (Join-Path $ProjectPath 'run\crash-reports'),
        (Join-Path $ProjectPath 'runs\client\crash-reports')
    ) | Select-Object -Unique

    $files = @()
    foreach ($candidateDir in $candidateDirs) {
        if (Test-Path $candidateDir) {
            $files += Get-ChildItem -Path $candidateDir -File -Filter '*.txt' -ErrorAction SilentlyContinue
        }
    }

    $files | Sort-Object FullName -Unique
}

function Get-ClientLogCandidates {
    param(
        [string]$ProjectPath
    )

    @(
        (Join-Path $ProjectPath 'logs\latest.log'),
        (Join-Path $ProjectPath 'logs\debug.log'),
        (Join-Path $ProjectPath 'run\logs\latest.log'),
        (Join-Path $ProjectPath 'run\logs\debug.log'),
        (Join-Path $ProjectPath 'runs\client\logs\latest.log'),
        (Join-Path $ProjectPath 'runs\client\logs\debug.log')
    ) | Select-Object -Unique
}

function Get-TailText {
    param(
        [string]$Path,
        [int]$LineCount = 40
    )

    if (-not $Path -or -not (Test-Path $Path)) {
        return ''
    }

    (Get-Content -Path $Path -Tail $LineCount -ErrorAction SilentlyContinue) -join [Environment]::NewLine
}

function Stop-ProcessTree {
    param(
        [int]$ProcessId
    )

    if ($ProcessId -le 0) {
        return
    }

    & taskkill.exe /PID $ProcessId /T /F | Out-Null
}

$root = Split-Path -Parent $PSScriptRoot
$projectPath = Join-Path $root ("versions\" + $Version + "\project")
$launcherScript = Join-Path $PSScriptRoot 'Invoke-Version.ps1'
$smokeReportDir = Join-Path $projectPath 'build\reports\easylan-smoke'
$stdoutPath = Join-Path $smokeReportDir 'runClient.stdout.log'
$stderrPath = Join-Path $smokeReportDir 'runClient.stderr.log'

if (-not (Test-Path $projectPath)) {
    throw "Missing version project: $projectPath"
}

if (-not (Test-Path $launcherScript)) {
    throw "Missing launcher script: $launcherScript"
}

if ($BuildFirst) {
    & $launcherScript -Version $Version -Task build -SkipServerCacheCheck:$SkipServerCacheCheck -GradleArgs $GradleArgs
}

New-Item -ItemType Directory -Force -Path $smokeReportDir | Out-Null

if (Test-Path $stdoutPath) {
    Remove-Item -LiteralPath $stdoutPath -Force
}

if (Test-Path $stderrPath) {
    Remove-Item -LiteralPath $stderrPath -Force
}

$baselineCrashReports = @(Get-CrashReportFiles -ProjectPath $projectPath | Select-Object -ExpandProperty FullName)
$logCandidates = Get-ClientLogCandidates -ProjectPath $projectPath

$launcherArgs = @(
    '-NoLogo',
    '-NoProfile',
    '-ExecutionPolicy',
    'Bypass',
    '-File',
    $launcherScript,
    '-Version',
    $Version,
    '-Task',
    'run'
)

if ($SkipServerCacheCheck) {
    $launcherArgs += '-SkipServerCacheCheck'
}

if ($GradleArgs) {
    $launcherArgs += '-GradleArgs'
    $launcherArgs += $GradleArgs
}

$launcherProcess = Start-Process -FilePath 'powershell.exe' `
    -ArgumentList $launcherArgs `
    -WorkingDirectory $projectPath `
    -PassThru `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath

$startedAt = Get-Date
$status = 'Failed'
$reason = ''
$clientLogPath = ''
$newCrashReports = @()

try {
    while ($true) {
        Start-Sleep -Seconds $PollSeconds
        $launcherProcess.Refresh()

        if (-not $clientLogPath) {
            $clientLogPath = $logCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
        }

        $newCrashReports = @(Get-CrashReportFiles -ProjectPath $projectPath | Where-Object { $baselineCrashReports -notcontains $_.FullName })
        if ($newCrashReports.Count -gt 0) {
            $reason = 'Detected new crash report during client smoke run.'
            break
        }

        $elapsedSeconds = [int]((Get-Date) - $startedAt).TotalSeconds
        if ($elapsedSeconds -ge $WarmupSeconds -and -not $launcherProcess.HasExited) {
            $status = 'ClientAlive'
            $reason = "Client stayed alive for $WarmupSeconds seconds."
            break
        }

        if ($launcherProcess.HasExited) {
            $reason = "Launcher exited before warmup window completed (exit code $($launcherProcess.ExitCode))."
            break
        }

        if ($elapsedSeconds -ge $TimeoutSeconds) {
            $reason = "Smoke run timed out after $TimeoutSeconds seconds."
            break
        }
    }
} finally {
    if (-not $KeepClientOpen -and -not $launcherProcess.HasExited) {
        Stop-ProcessTree -ProcessId $launcherProcess.Id
        Start-Sleep -Seconds 2
        $launcherProcess.Refresh()
    }
}

$result = [PSCustomObject]@{
    Version = $Version
    Status = $status
    Reason = $reason
    WarmupSeconds = $WarmupSeconds
    TimeoutSeconds = $TimeoutSeconds
    LauncherPid = $launcherProcess.Id
    LauncherExited = $launcherProcess.HasExited
    ExitCode = if ($launcherProcess.HasExited) { $launcherProcess.ExitCode } else { $null }
    ClientLog = $clientLogPath
    StdoutLog = $stdoutPath
    StderrLog = $stderrPath
    CrashReports = [string[]]($newCrashReports | Select-Object -ExpandProperty FullName)
    StdoutTail = Get-TailText -Path $stdoutPath
    StderrTail = Get-TailText -Path $stderrPath
    ClientLogTail = Get-TailText -Path $clientLogPath
}

$result

if ($status -ne 'ClientAlive' -and -not $NoThrow) {
    throw ("Client smoke failed for {0}: {1}" -f $Version, $reason)
}
