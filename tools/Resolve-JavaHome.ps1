param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$javaRoot = if ($env:EASYLAN_JAVA_ROOT) { $env:EASYLAN_JAVA_ROOT } else { 'F:\Java' }
$gradleUserHome = if ($env:EASYLAN_GRADLE_USER_HOME) { $env:EASYLAN_GRADLE_USER_HOME } else { 'H:\gradle' }
$root = Split-Path -Parent $PSScriptRoot
$projectPath = Join-Path $root ("versions\" + $Version + "\project")
$gradlePropertiesPath = Join-Path $projectPath 'gradle.properties'
$buildGradlePath = Join-Path $projectPath 'build.gradle'

function Get-ConfiguredJavaVersion {
    param(
        [string]$GradlePropertiesPath,
        [string]$BuildGradlePath
    )

    if (Test-Path $GradlePropertiesPath) {
        $javaVersionLine = Get-Content $GradlePropertiesPath | Where-Object { $_ -match '^\s*java_version\s*=\s*(\d+)\s*$' } | Select-Object -First 1
        if ($javaVersionLine -match '(\d+)') {
            return [int]$Matches[1]
        }
    }

    if (Test-Path $BuildGradlePath) {
        $buildContent = Get-Content $BuildGradlePath -Raw
        if ($buildContent -match 'JavaLanguageVersion\.of\((\d+)\)') {
            return [int]$Matches[1]
        }
    }

    return $null
}

$requestedJavaVersion = Get-ConfiguredJavaVersion -GradlePropertiesPath $gradlePropertiesPath -BuildGradlePath $buildGradlePath

if (-not $requestedJavaVersion) {
    $requestedJavaVersion = switch -Regex ($Version) {
        '^1\.16\.(4|5)$' { 8; break }
        '^1\.(17\.1|18\.2|19\.2|19\.4|20\.1)$' { 17; break }
        '^1\.(20\.6|21\.1|21\.5|21\.11)$' { 21; break }
        default { 8 }
    }
}

$javaHome = switch ($requestedJavaVersion) {
    7 { Join-Path $javaRoot 'jdk7'; break }
    8 { Join-Path $javaRoot 'jdk8'; break }
    11 { Join-Path $javaRoot 'zulu11'; break }
    16 { Join-Path $javaRoot 'zulu17'; break }
    17 { Join-Path $javaRoot 'zulu17'; break }
    21 { Join-Path $javaRoot 'zulu21'; break }
    25 { Join-Path $javaRoot 'zulu25'; break }
    default { Join-Path $javaRoot 'jdk8' }
}

if (-not (Test-Path $javaHome)) {
    throw ("Missing JAVA_HOME for version {0}: {1}" -f $Version, $javaHome)
}

if (-not (Test-Path $gradleUserHome)) {
    throw ("Missing Gradle user home: {0}" -f $gradleUserHome)
}

[PSCustomObject]@{
    Version = $Version
    RequestedJavaVersion = $requestedJavaVersion
    JavaHome = $javaHome
    GradleUserHome = $gradleUserHome
    JavaExecutable = Join-Path $javaHome 'bin\java.exe'
}
