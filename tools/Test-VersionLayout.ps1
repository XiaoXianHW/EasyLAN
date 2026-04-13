param(
    [string]$Version,
    [switch]$SkipJavaResolution,
    [switch]$NoThrow
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

$results = @()

foreach ($versionDir in $versionDirs) {
    $errors = New-Object System.Collections.Generic.List[string]
    $projectPath = Join-Path $versionDir.FullName 'project'
    $buildFile = Join-Path $projectPath 'build.gradle'
    $wrapperBat = Join-Path $projectPath 'gradlew.bat'
    $wrapperJar = Join-Path $projectPath 'gradle\wrapper\gradle-wrapper.jar'

    if (-not (Test-Path $projectPath)) {
        $errors.Add("Missing version project directory: $projectPath")
    }

    if (-not (Test-Path $buildFile)) {
        $errors.Add("Missing build.gradle: $buildFile")
    }

    if (-not (Test-Path $wrapperBat)) {
        $errors.Add("Missing Gradle wrapper bat: $wrapperBat")
    }

    if (-not (Test-Path $wrapperJar)) {
        $errors.Add("Missing Gradle wrapper jar: $wrapperJar")
    }

    try {
        & (Join-Path $PSScriptRoot 'Sync-SharedSources.ps1') -Version $versionDir.Name | Out-Null
    } catch {
        $errors.Add($_.Exception.Message)
    }

    if (-not $SkipJavaResolution) {
        try {
            $javaInfo = & (Join-Path $PSScriptRoot 'Resolve-JavaHome.ps1') -Version $versionDir.Name
            if (-not (Test-Path $javaInfo.JavaExecutable)) {
                $errors.Add("Missing Java executable: $($javaInfo.JavaExecutable)")
            }
        } catch {
            $errors.Add($_.Exception.Message)
        }
    }

    $results += [PSCustomObject]@{
        Version = $versionDir.Name
        Status = if ($errors.Count -eq 0) { 'Passed' } else { 'Failed' }
        Project = $projectPath
        ErrorCount = $errors.Count
        Errors = [string[]]$errors.ToArray()
    }
}

$results

$failed = $results | Where-Object { $_.Status -eq 'Failed' }
if ($failed -and -not $NoThrow) {
    $failedVersions = ($failed | Select-Object -ExpandProperty Version) -join ', '
    throw "Layout validation failed for: $failedVersions"
}
