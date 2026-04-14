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

function Test-AssetFile {
    param(
        [string]$Path,
        [string]$ExpectedHash,
        [Nullable[int64]]$ExpectedSize
    )

    if (-not (Test-Path $Path)) {
        return $false
    }

    $item = Get-Item -LiteralPath $Path
    if ($ExpectedSize -and $item.Length -ne $ExpectedSize.Value) {
        return $false
    }

    $actualHash = (Get-FileHash -LiteralPath $Path -Algorithm SHA1).Hash.ToLowerInvariant()
    return $actualHash -eq $ExpectedHash.ToLowerInvariant()
}

function Save-AssetFromUrl {
    param(
        [string]$Url,
        [string]$Destination,
        [string]$ExpectedHash,
        [Nullable[int64]]$ExpectedSize
    )

    $tempPath = $Destination + '.download'
    if (Test-Path $tempPath) {
        Remove-Item -LiteralPath $tempPath -Force
    }

    Invoke-WebRequest -Uri $Url -OutFile $tempPath -UseBasicParsing -TimeoutSec 120
    if (-not (Test-AssetFile -Path $tempPath -ExpectedHash $ExpectedHash -ExpectedSize $ExpectedSize)) {
        Remove-Item -LiteralPath $tempPath -Force
        throw "Downloaded asset validation failed: $ExpectedHash"
    }

    Move-Item -LiteralPath $tempPath -Destination $Destination -Force
}

if (-not $GradleUserHome) {
    $javaInfo = & (Join-Path $PSScriptRoot 'Resolve-JavaHome.ps1') -Version $Version
    $GradleUserHome = $javaInfo.GradleUserHome
}

$assetIndexPath = Join-Path $GradleUserHome ("caches\minecraft\assets\indexes\{0}.json" -f $Version)
if (-not (Test-Path $assetIndexPath)) {
    return [PSCustomObject]@{
        Version = $Version
        Status = 'Skipped'
        Reason = 'Missing asset index.'
        Checked = 0
        Reused = 0
        Copied = 0
        Downloaded = 0
    }
}

$assetIndex = Get-Content -LiteralPath $assetIndexPath -Raw | ConvertFrom-Json
if (-not $assetIndex.objects) {
    return [PSCustomObject]@{
        Version = $Version
        Status = 'Skipped'
        Reason = 'Asset index has no objects node.'
        Checked = 0
        Reused = 0
        Copied = 0
        Downloaded = 0
    }
}

$sourceRoots = @(
    (Join-Path $env:APPDATA '.minecraft\assets\objects')
) | Where-Object { $_ -and (Test-Path $_) }

$assetRoot = Join-Path $GradleUserHome 'caches\minecraft\assets\objects'
New-Item -ItemType Directory -Force -Path $assetRoot | Out-Null

$checked = 0
$reused = 0
$copied = 0
$downloaded = 0

foreach ($assetProperty in $assetIndex.objects.PSObject.Properties) {
    $checked += 1

    $assetInfo = $assetProperty.Value
    $hash = [string]$assetInfo.hash
    if (-not $hash -or $hash.Length -lt 2) {
        continue
    }

    $size = $null
    if ($assetInfo.PSObject.Properties.Name -contains 'size') {
        $size = [int64]$assetInfo.size
    }

    $prefix = $hash.Substring(0, 2)
    $targetDir = Join-Path $assetRoot $prefix
    $targetPath = Join-Path $targetDir $hash

    if (Test-AssetFile -Path $targetPath -ExpectedHash $hash -ExpectedSize $size) {
        $reused += 1
        continue
    }

    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    $copiedFromCache = $false
    foreach ($sourceRoot in $sourceRoots) {
        $sourcePath = Join-Path (Join-Path $sourceRoot $prefix) $hash
        if (-not (Test-AssetFile -Path $sourcePath -ExpectedHash $hash -ExpectedSize $size)) {
            continue
        }

        Copy-Item -LiteralPath $sourcePath -Destination $targetPath -Force
        $copied += 1
        $copiedFromCache = $true
        break
    }

    if ($copiedFromCache) {
        continue
    }

    $url = "https://resources.download.minecraft.net/{0}/{1}" -f $prefix, $hash
    Save-AssetFromUrl -Url $url -Destination $targetPath -ExpectedHash $hash -ExpectedSize $size
    $downloaded += 1

    if (($checked % 100) -eq 0) {
        Write-Host ("[EasyLAN] Asset cache progress {0}/{1}" -f $checked, $assetIndex.objects.PSObject.Properties.Count) -ForegroundColor DarkCyan
    }
}

[PSCustomObject]@{
    Version = $Version
    Status = 'Healthy'
    Reason = 'Assets cache checked.'
    Checked = $checked
    Reused = $reused
    Copied = $copied
    Downloaded = $downloaded
}
