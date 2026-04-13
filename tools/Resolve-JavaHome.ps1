param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$javaRoot = if ($env:EASYLAN_JAVA_ROOT) { $env:EASYLAN_JAVA_ROOT } else { 'F:\Java' }
$gradleUserHome = if ($env:EASYLAN_GRADLE_USER_HOME) { $env:EASYLAN_GRADLE_USER_HOME } else { 'H:\gradle' }

$javaHome = switch -Regex ($Version) {
    '^1\.16\.(4|5)$' { Join-Path $javaRoot 'jdk8'; break }
    '^1\.(17\.1|18\.2|19\.2|19\.4|20\.1)$' { Join-Path $javaRoot 'zulu17'; break }
    '^1\.(20\.6|21\.1)$' { Join-Path $javaRoot 'zulu21'; break }
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
    JavaHome = $javaHome
    GradleUserHome = $gradleUserHome
    JavaExecutable = Join-Path $javaHome 'bin\java.exe'
}
