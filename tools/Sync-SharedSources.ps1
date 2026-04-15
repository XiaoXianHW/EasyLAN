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

$projects | ForEach-Object {
    $buildFile = Join-Path $_.Project 'build.gradle'
    if (-not (Test-Path $buildFile)) {
        throw "Missing build.gradle: $buildFile"
    }

    $content = Get-Content $buildFile -Raw

    $javaEntries = [regex]::Matches($content, "new File\(repoRoot, '([^']+/src/main/java)'\)") |
        ForEach-Object { $_.Groups[1].Value } |
        Where-Object { $_ -like 'shared/*' } |
        Select-Object -Unique

    $resourceEntries = [regex]::Matches($content, "new File\(repoRoot, '([^']+/src/main/resources)'\)") |
        ForEach-Object { $_.Groups[1].Value } |
        Where-Object { $_ -like 'shared/*' } |
        Select-Object -Unique

    if ($javaEntries -notcontains 'shared/core/src/main/java') {
        throw "Shared source wiring is incomplete in $buildFile : shared/core/src/main/java"
    }

    if ($resourceEntries -notcontains 'shared/core/src/main/resources') {
        throw "Shared source wiring is incomplete in $buildFile : shared/core/src/main/resources"
    }

    $groupJavaEntries = $javaEntries | Where-Object { $_ -ne 'shared/core/src/main/java' }
    $groupResourceEntries = $resourceEntries | Where-Object { $_ -ne 'shared/core/src/main/resources' }

    if (-not $groupJavaEntries) {
        throw "Shared source wiring is incomplete in $buildFile : missing loader-specific shared java source"
    }

    if (-not $groupResourceEntries) {
        throw "Shared source wiring is incomplete in $buildFile : missing loader-specific shared resources source"
    }

    foreach ($entry in $javaEntries) {
        $absolutePath = Join-Path $root $entry
        if (-not (Test-Path $absolutePath)) {
            throw "Referenced shared source path does not exist in $buildFile : $entry"
        }
    }

    [PSCustomObject]@{
        Version = $_.Version
        BuildFile = $buildFile
        SharedJava = [string[]]$javaEntries
        SharedResources = [string[]]$resourceEntries
        Mode = 'direct-source-set'
    }
}
