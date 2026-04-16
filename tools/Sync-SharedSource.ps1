param(
    [string]$Profile = 'all',
    [string]$Branches = '',
    [switch]$Push,
    [string]$MapPath = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptRoot

. (Join-Path $scriptRoot 'SharedSyncCommon.ps1')

function Get-RepositoryUrl {
    if (
        -not [string]::IsNullOrWhiteSpace($env:GITHUB_TOKEN) -and
        -not [string]::IsNullOrWhiteSpace($env:GITHUB_REPOSITORY)
    ) {
        return "https://x-access-token:$($env:GITHUB_TOKEN)@github.com/$($env:GITHUB_REPOSITORY).git"
    }

    return (git -C $repoRoot remote get-url origin).Trim()
}

$resolvedMapPath = if ([string]::IsNullOrWhiteSpace($MapPath)) {
    Join-Path $repoRoot 'tools/shared-sync-map.json'
} else {
    $MapPath
}

$map = Get-SharedSyncMap -MapPath $resolvedMapPath
$targetBranches = @(Resolve-SharedBranches -Map $map -Profile $Profile -Branches $Branches)
$repoUrl = Get-RepositoryUrl
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) 'EasyLAN-shared-sync'

if (Test-Path $tempRoot) {
    Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
}

New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null

try {
    foreach ($branch in $targetBranches) {
        $branchPath = Join-Path $tempRoot ($branch -replace '[^A-Za-z0-9._-]', '_')

        & git clone --depth 1 --single-branch --branch $branch $repoUrl $branchPath | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to clone branch: $branch"
        }

        & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot 'tools/Apply-SharedSource.ps1') `
            -SourceRepositoryRoot $repoRoot `
            -TargetRepositoryRoot $branchPath `
            -Branch $branch `
            -MapPath $resolvedMapPath

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to apply shared source for branch: $branch"
        }

        & git -C $branchPath add -A
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to stage shared-source changes for $branch"
        }

        & git -C $branchPath diff --cached --quiet
        if ($LASTEXITCODE -eq 0) {
            Write-Host "No staged shared-source changes for $branch"
            continue
        }

        if ($LASTEXITCODE -ne 1) {
            throw "Failed to evaluate staged shared-source changes for $branch"
        }

        & git -C $branchPath -c user.name='github-actions[bot]' -c user.email='41898282+github-actions[bot]@users.noreply.github.com' `
            commit -m 'Sync shared-source from main [skip ci]' | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to commit shared-source changes for $branch"
        }

        Write-Host "Committed shared-source changes for $branch"

        if ($Push) {
            & git -C $branchPath push origin $branch | Out-Null
            if ($LASTEXITCODE -ne 0) {
                throw "Failed to push shared-source changes for $branch"
            }

            Write-Host "Pushed shared-source changes for $branch"
        }
    }
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
