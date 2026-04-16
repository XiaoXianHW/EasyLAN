param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRepositoryRoot,
    [Parameter(Mandatory = $true)]
    [string]$TargetRepositoryRoot,
    [Parameter(Mandatory = $true)]
    [string]$Branch,
    [string]$MapPath = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $scriptRoot 'SharedSyncCommon.ps1')

$resolvedMapPath = if ([string]::IsNullOrWhiteSpace($MapPath)) {
    Join-Path $SourceRepositoryRoot 'tools/shared-sync-map.json'
} else {
    $MapPath
}

$map = Get-SharedSyncMap -MapPath $resolvedMapPath
$targets = @(Get-BranchSharedTargets -Map $map -Branch $Branch)

if ($targets.Count -eq 0) {
    Write-Host "No shared-source mapping found for $Branch"
    exit 0
}

foreach ($target in $targets) {
    $sourcePath = Join-Path $SourceRepositoryRoot $target.source
    $destinationPath = Join-Path $TargetRepositoryRoot $target.destination
    Sync-DirectoryContents -SourcePath $sourcePath -DestinationPath $destinationPath
    Write-Host "Applied $($target.name) -> $($target.destination) for $Branch"
}
