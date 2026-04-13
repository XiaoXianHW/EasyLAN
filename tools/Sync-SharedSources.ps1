param(
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$projects = & (Join-Path $PSScriptRoot 'Get-VersionProjects.ps1') -Version $Version

if (-not $projects) {
    throw "No version projects matched."
}

$expectedJava = @(
    "new File(repoRoot, 'shared/core/src/main/java')",
    "new File(repoRoot, 'shared/forge-group/src/main/java')"
)

$expectedResources = @(
    "new File(repoRoot, 'shared/core/src/main/resources')",
    "new File(repoRoot, 'shared/forge-group/src/main/resources')"
)

$projects | ForEach-Object {
    $buildFile = Join-Path $_.Project 'build.gradle'
    if (-not (Test-Path $buildFile)) {
        throw "Missing build.gradle: $buildFile"
    }

    $content = Get-Content $buildFile -Raw

    foreach ($entry in $expectedJava + $expectedResources) {
        if (-not $content.Contains($entry)) {
            throw "Shared source wiring is incomplete in $buildFile : $entry"
        }
    }

    [PSCustomObject]@{
        Version = $_.Version
        BuildFile = $buildFile
        SharedCore = Join-Path $root 'shared/core/src/main/java'
        SharedForgeGroup = Join-Path $root 'shared/forge-group/src/main/java'
        Mode = 'direct-source-set'
    }
}
