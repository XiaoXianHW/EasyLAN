param(
    [ValidateSet('Layout', 'Build', 'Smoke', 'Full')]
    [string]$Mode = 'Layout',

    [string]$Version,

    [int]$WarmupSeconds = 90,
    [int]$TimeoutSeconds = 300,
    [switch]$KeepClientOpen,
    [switch]$SkipJavaResolution,
    [switch]$SkipServerCacheCheck,
    [Alias('ExtraArgs')]
    [string[]]$GradleArgs,
    [switch]$StopOnError
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$versionsRoot = Join-Path $root 'versions'

if (-not (Test-Path $versionsRoot)) {
    throw "Missing versions directory: $versionsRoot"
}

$versionDirs = Get-ChildItem -Path $versionsRoot -Directory | Sort-Object Name
if ($Version) {
    $versionDirs = $versionDirs | Where-Object { $_.Name -eq $Version }
}

if (-not $versionDirs) {
    throw "No version projects matched."
}

if ($KeepClientOpen -and $versionDirs.Count -ne 1) {
    throw 'KeepClientOpen can only be used when a single version is targeted.'
}

$results = @()

foreach ($versionDir in $versionDirs) {
    $row = [ordered]@{
        Version = $versionDir.Name
        Mode = $Mode
        Layout = 'Skipped'
        Build = 'Skipped'
        Smoke = 'Skipped'
        Status = 'Passed'
        Notes = ''
    }

    try {
        $layoutResult = & (Join-Path $PSScriptRoot 'Test-VersionLayout.ps1') -Version $versionDir.Name -SkipJavaResolution:$SkipJavaResolution -NoThrow
        $layoutStatus = ($layoutResult | Select-Object -Last 1).Status
        $row.Layout = $layoutStatus

        if ($layoutStatus -ne 'Passed') {
            $layoutErrors = ($layoutResult | Select-Object -Last 1).Errors -join ' | '
            throw "Layout validation failed: $layoutErrors"
        }

        if ($Mode -eq 'Build' -or $Mode -eq 'Full') {
            & (Join-Path $PSScriptRoot 'Invoke-Version.ps1') -Version $versionDir.Name -Task build -GradleArgs $GradleArgs -SkipServerCacheCheck:$SkipServerCacheCheck
            $row.Build = 'Passed'
        }

        if ($Mode -eq 'Smoke' -or $Mode -eq 'Full') {
            $smokeResult = & (Join-Path $PSScriptRoot 'Test-VersionSmoke.ps1') `
                -Version $versionDir.Name `
                -WarmupSeconds $WarmupSeconds `
                -TimeoutSeconds $TimeoutSeconds `
                -KeepClientOpen:$KeepClientOpen `
                -SkipServerCacheCheck:$SkipServerCacheCheck `
                -GradleArgs $GradleArgs `
                -NoThrow
            $row.Smoke = $smokeResult.Status

            if ($smokeResult.Status -ne 'ClientAlive') {
                throw $smokeResult.Reason
            }
        }
    } catch {
        $row.Status = 'Failed'
        $row.Notes = $_.Exception.Message

        if ($StopOnError) {
            $results += [PSCustomObject]$row
            break
        }
    }

    $results += [PSCustomObject]$row
}

$results

$failed = $results | Where-Object { $_.Status -eq 'Failed' }
if ($failed) {
    $failedVersions = ($failed | Select-Object -ExpandProperty Version) -join ', '
    throw "Version group checks failed for: $failedVersions"
}
