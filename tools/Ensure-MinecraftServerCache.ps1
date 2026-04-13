param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [string]$GradleUserHome
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

[Net.ServicePointManager]::SecurityProtocol = `
    [Net.SecurityProtocolType]::Tls12 -bor `
    [Net.SecurityProtocolType]::Tls11 -bor `
    [Net.SecurityProtocolType]::Tls

$knownVersions = @{
    '1.16.4' = @{
        McpConfigVersion = '1.16.4-20201102.104115'
    }
    '1.16.5' = @{
        McpConfigVersion = '1.16.5-20210115.111550'
    }
    '1.17.1' = @{
        McpConfigVersion = '1.17.1-20210706.113038'
        ServerSha1 = 'a16d67e5807f57fc4e550299cf20226194497dc2'
        ServerSize = 43626592
    }
    '1.18.2' = @{
        McpConfigVersion = '1.18.2-20220404.173914'
    }
}

function New-Result {
    param(
        [string]$Status,
        [string]$Path,
        [string]$Reason
    )

    [PSCustomObject]@{
        Version = $Version
        Status = $Status
        Path = $Path
        Reason = $Reason
    }
}

function Get-CacheMetadata {
    param(
        [string]$TargetVersion,
        [string]$VersionJsonPath,
        [hashtable]$KnownVersion
    )

    if (Test-Path $VersionJsonPath) {
        $json = Get-Content -Raw $VersionJsonPath | ConvertFrom-Json
        if ($json.downloads -and $json.downloads.server) {
            return @{
                Url = $json.downloads.server.url
                Sha1 = $json.downloads.server.sha1
                Size = [int64]$json.downloads.server.size
                Source = 'cache'
            }
        }
    }

    if ($KnownVersion -and $KnownVersion.ContainsKey('ServerSha1')) {
        return @{
            Url = 'https://piston-data.mojang.com/v1/objects/' + $KnownVersion.ServerSha1 + '/server.jar'
            Sha1 = $KnownVersion.ServerSha1
            Size = [int64]$KnownVersion.ServerSize
            Source = 'fallback'
        }
    }

    $manifestUrls = @(
        'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json',
        'https://launchermeta.mojang.com/mc/game/version_manifest_v2.json'
    )

    foreach ($manifestUrl in $manifestUrls) {
        try {
            $manifest = Invoke-RestMethod -Uri $manifestUrl -TimeoutSec 30
            $versionEntry = $manifest.versions | Where-Object { $_.id -eq $TargetVersion } | Select-Object -First 1
            if (-not $versionEntry) {
                continue
            }

            $versionCacheDir = Split-Path -Parent $VersionJsonPath
            if (-not (Test-Path $versionCacheDir)) {
                New-Item -ItemType Directory -Force -Path $versionCacheDir | Out-Null
            }

            $tempVersionJson = $VersionJsonPath + '.download'
            if (Test-Path $tempVersionJson) {
                Remove-Item -LiteralPath $tempVersionJson -Force
            }

            Invoke-WebRequest -Uri $versionEntry.url -OutFile $tempVersionJson -UseBasicParsing -TimeoutSec 60
            Move-Item -LiteralPath $tempVersionJson -Destination $VersionJsonPath -Force

            $json = Get-Content -Raw $VersionJsonPath | ConvertFrom-Json
            if ($json.downloads -and $json.downloads.server) {
                return @{
                    Url = $json.downloads.server.url
                    Sha1 = $json.downloads.server.sha1
                    Size = [int64]$json.downloads.server.size
                    Source = 'manifest'
                }
            }
        } catch {
            continue
        }
    }

    return $null
}

function Test-ServerCacheFile {
    param(
        [string]$Path,
        [string]$ExpectedSha1,
        [int64]$ExpectedSize
    )

    if (-not (Test-Path $Path)) {
        return $false
    }

    $item = Get-Item $Path
    if ($ExpectedSize -gt 0 -and $item.Length -ne $ExpectedSize) {
        return $false
    }

    $actualSha1 = (Get-FileHash -LiteralPath $Path -Algorithm SHA1).Hash.ToLowerInvariant()
    return $actualSha1 -eq $ExpectedSha1.ToLowerInvariant()
}

function Download-ServerCacheFile {
    param(
        [string]$Url,
        [string]$Destination,
        [string]$ExpectedSha1,
        [int64]$ExpectedSize
    )

    $tempPath = $Destination + '.codex-download'
    $attempts = 3

    for ($attempt = 1; $attempt -le $attempts; $attempt++) {
        if (Test-Path $tempPath) {
            Remove-Item -LiteralPath $tempPath -Force
        }

        try {
            Invoke-WebRequest -Uri $Url -OutFile $tempPath -UseBasicParsing -TimeoutSec 180

            if (-not (Test-ServerCacheFile -Path $tempPath -ExpectedSha1 $ExpectedSha1 -ExpectedSize $ExpectedSize)) {
                throw 'Downloaded server.jar validation failed.'
            }

            Move-Item -LiteralPath $tempPath -Destination $Destination -Force
            return
        } catch {
            if (Test-Path $tempPath) {
                Remove-Item -LiteralPath $tempPath -Force
            }

            if ($attempt -eq $attempts) {
                throw
            }

            Start-Sleep -Seconds (5 * $attempt)
        }
    }
}

if (-not $GradleUserHome) {
    $javaInfo = & (Join-Path $PSScriptRoot 'Resolve-JavaHome.ps1') -Version $Version
    $GradleUserHome = $javaInfo.GradleUserHome
}

$knownVersion = $knownVersions[$Version]
if (-not $knownVersion) {
    return New-Result -Status 'Skipped' -Path '' -Reason 'Unsupported version.'
}

$versionJsonPath = Join-Path $GradleUserHome ('caches\forge_gradle\minecraft_repo\versions\' + $Version + '\version.json')
$metadata = Get-CacheMetadata -TargetVersion $Version -VersionJsonPath $versionJsonPath -KnownVersion $knownVersion
if (-not $metadata) {
    return New-Result -Status 'Skipped' -Path '' -Reason 'Missing server metadata.'
}

$targetDir = Join-Path $GradleUserHome ('caches\forge_gradle\mcp_repo\de\oceanlabs\mcp\mcp_config\' + $knownVersion.McpConfigVersion + '\joined\downloadServer')
$targetPath = Join-Path $targetDir 'server.jar'
$newPath = Join-Path $targetDir 'server.jar.new'

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

if (Test-ServerCacheFile -Path $targetPath -ExpectedSha1 $metadata.Sha1 -ExpectedSize $metadata.Size) {
    return New-Result -Status 'Healthy' -Path $targetPath -Reason ('Validated from ' + $metadata.Source + '.')
}

if (Test-Path $newPath) {
    try {
        Remove-Item -LiteralPath $newPath -Force
    } catch {
        throw ('Cannot reset incomplete cache file: ' + $newPath)
    }
}

if (Test-Path $targetPath) {
    Remove-Item -LiteralPath $targetPath -Force
}

Download-ServerCacheFile -Url $metadata.Url -Destination $targetPath -ExpectedSha1 $metadata.Sha1 -ExpectedSize $metadata.Size
return New-Result -Status 'Repaired' -Path $targetPath -Reason ('Downloaded from ' + $metadata.Source + '.')
