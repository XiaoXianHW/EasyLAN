param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [Parameter(Mandatory = $false)]
    [string]$Task = 'build',

    [Alias('ExtraArgs')]
    [string[]]$GradleArgs,

    [switch]$SkipServerCacheCheck,
    [switch]$SkipAssetCacheCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$projectPath = Join-Path $root ("versions\" + $Version + "\project")
$wrapper = Join-Path $projectPath 'gradlew.bat'
$javaInfo = & (Join-Path $PSScriptRoot 'Resolve-JavaHome.ps1') -Version $Version

if (-not (Test-Path $projectPath)) {
    throw "Missing version project: $projectPath"
}

if (-not (Test-Path $wrapper)) {
    throw "Missing Gradle wrapper: $wrapper"
}

$taskAliases = @{
    build = 'build'
    run = 'runClient'
    client = 'runClient'
}

if ($taskAliases.ContainsKey($Task)) {
    $Task = $taskAliases[$Task]
}

Write-Host ("[EasyLAN] Running " + $Version + " -> " + $Task) -ForegroundColor Cyan
Push-Location $projectPath
try {
    $previousJavaHome = $env:JAVA_HOME
    $previousGradleHome = $env:GRADLE_USER_HOME
    $previousPath = $env:PATH

    $env:JAVA_HOME = $javaInfo.JavaHome
    $env:GRADLE_USER_HOME = $javaInfo.GradleUserHome
    $env:PATH = (Join-Path $javaInfo.JavaHome 'bin') + ';' + $env:PATH

    if (-not $SkipServerCacheCheck) {
        $cacheResult = & (Join-Path $PSScriptRoot 'Ensure-MinecraftServerCache.ps1') -Version $Version -GradleUserHome $javaInfo.GradleUserHome
        if ($cacheResult -and $cacheResult.Status -ne 'Skipped') {
            Write-Host ("[EasyLAN] Server cache " + $cacheResult.Status + " -> " + $cacheResult.Path) -ForegroundColor DarkCyan
        }
    }

    $assetTasks = @('runClient', 'debugClient', 'runServer', 'debugServer', 'setupDevWorkspace', 'setupDecompWorkspace')
    if (-not $SkipAssetCacheCheck -and $assetTasks -contains $Task) {
        $assetResult = & (Join-Path $PSScriptRoot 'Ensure-MinecraftAssetsCache.ps1') -Version $Version -GradleUserHome $javaInfo.GradleUserHome
        if ($assetResult -and $assetResult.Status -ne 'Skipped') {
            Write-Host ("[EasyLAN] Asset cache " + $assetResult.Status + " -> reused " + $assetResult.Reused + ", copied " + $assetResult.Copied + ", downloaded " + $assetResult.Downloaded) -ForegroundColor DarkCyan
        }
    }

    & $wrapper $Task @GradleArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle task failed for $Version -> $Task with exit code $LASTEXITCODE"
    }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:GRADLE_USER_HOME = $previousGradleHome
    $env:PATH = $previousPath
    Pop-Location
}
