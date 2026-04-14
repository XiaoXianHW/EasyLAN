param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$javaRoot = if ($env:EASYLAN_JAVA_ROOT) { $env:EASYLAN_JAVA_ROOT } else { 'F:\Java' }
$configuredGradleHome = if ($env:EASYLAN_GRADLE_USER_HOME) { $env:EASYLAN_GRADLE_USER_HOME } else { 'H:\gradle' }
$nestedGradleHome = Join-Path $configuredGradleHome '.gradle'

function Get-GradleHomeScore {
    param(
        [string]$GradleHomePath,
        [string]$TargetVersion
    )

    if (-not (Test-Path $GradleHomePath)) {
        return -1
    }

    $score = 0

    if (Test-Path (Join-Path $GradleHomePath 'wrapper\dists')) {
        $score += 1
    }

    $clientJar = Join-Path $GradleHomePath ("caches\minecraft\net\minecraft\minecraft\{0}\minecraft-{0}.jar" -f $TargetVersion)
    if (Test-Path $clientJar) {
        $score += 5
    }

    $serverJar = Join-Path $GradleHomePath ("caches\minecraft\net\minecraft\minecraft_server\{0}\minecraft_server-{0}.jar" -f $TargetVersion)
    if (Test-Path $serverJar) {
        $score += 3
    }

    $forgeRoot = Join-Path $GradleHomePath 'caches\minecraft\net\minecraftforge\forge'
    if (Test-Path $forgeRoot) {
        $forgeVersionDir = Get-ChildItem -Path $forgeRoot -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like ($TargetVersion + '-*') } |
            Select-Object -First 1
        if ($forgeVersionDir) {
            $score += 4
        }
    }

    return $score
}

$gradleUserHome = $configuredGradleHome
if (-not $env:EASYLAN_GRADLE_USER_HOME) {
    $configuredScore = Get-GradleHomeScore -GradleHomePath $configuredGradleHome -TargetVersion $Version
    $nestedScore = Get-GradleHomeScore -GradleHomePath $nestedGradleHome -TargetVersion $Version
    if ($nestedScore -gt $configuredScore) {
        $gradleUserHome = $nestedGradleHome
    }
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
