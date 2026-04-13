param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$javaRoot = if ($env:EASYLAN_JAVA_ROOT) { $env:EASYLAN_JAVA_ROOT } else { 'F:\Java' }
$configuredGradleHome = if ($env:EASYLAN_GRADLE_USER_HOME) { $env:EASYLAN_GRADLE_USER_HOME } else { 'H:\gradle' }
$nestedGradleHome = Join-Path $configuredGradleHome '.gradle'

$gradleUserHome = if (
    -not $env:EASYLAN_GRADLE_USER_HOME -and
    (Test-Path (Join-Path $nestedGradleHome 'wrapper\dists')) -and
    -not (Test-Path (Join-Path $configuredGradleHome 'wrapper\dists'))
) {
    $nestedGradleHome
} else {
    $configuredGradleHome
}

$javaHome = switch -Regex ($Version) {
    '^1\.(7\.2|7\.10|8|8\.9|9\.4|10\.2|11\.2)$' { Join-Path $javaRoot 'jdk8'; break }
    default { Join-Path $javaRoot 'jdk8' }
}

if (-not (Test-Path $javaHome)) {
    throw ("Missing JAVA_HOME for version {0}: {1}" -f $Version, $javaHome)
}

if (-not (Test-Path $gradleUserHome)) {
    New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null
}

[PSCustomObject]@{
    Version = $Version
    JavaHome = $javaHome
    GradleUserHome = $gradleUserHome
    JavaExecutable = Join-Path $javaHome 'bin\java.exe'
}
