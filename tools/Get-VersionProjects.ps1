param(
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$versionsRoot = Join-Path $root 'versions'

if (-not (Test-Path $versionsRoot)) {
    throw "Missing versions directory: $versionsRoot"
}

$projects = Get-ChildItem -Path $versionsRoot -Directory | Sort-Object Name

if ($Version) {
    $projects = $projects | Where-Object { $_.Name -eq $Version }
}

$projects | ForEach-Object {
    $projectPath = Join-Path $_.FullName 'project'
    $wrapperPath = Join-Path $projectPath 'gradlew.bat'
    $javaInfo = & (Join-Path $PSScriptRoot 'Resolve-JavaHome.ps1') -Version $_.Name
    [PSCustomObject]@{
        Version = $_.Name
        Project = $projectPath
        Wrapper = $wrapperPath
        Exists = Test-Path $projectPath
        JavaHome = $javaInfo.JavaHome
        GradleUserHome = $javaInfo.GradleUserHome
    }
}
