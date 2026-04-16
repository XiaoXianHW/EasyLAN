param(
    [string]$Profile = 'all',
    [string]$Branches = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$activeBranchGroups = [ordered]@{
    forge = @(
        'forge-1.7.2-1.11.2',
        'forge-1.12.2',
        'forge-1.13.2-1.15.2',
        'forge-1.16.4-1.18.2',
        'forge-1.19.2-1.21.11'
    )
    fabric = @(
        'fabric-1.14.4-1.15.2',
        'fabric-1.16.4-1.16.5',
        'fabric-1.17.1-1.20.1',
        'fabric-1.20.6-1.21.11'
    )
    neoforge = @(
        'neoforge-1.20.1-1.21.11'
    )
}

$allBranches = @(
    $activeBranchGroups.forge +
    $activeBranchGroups.fabric +
    $activeBranchGroups.neoforge
)

function Get-TargetBranches {
    param(
        [string]$RequestedProfile,
        [string]$RequestedBranches
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedBranches)) {
        $customBranches = @(
            $RequestedBranches -split '[,\s]+' |
                Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
                ForEach-Object { $_.Trim() } |
                Select-Object -Unique
        )

        if ($customBranches.Count -eq 0) {
            throw 'Custom branch list is empty.'
        }

        foreach ($branch in $customBranches) {
            if ($branch -like 'z-acrhive-*') {
                throw "Archive branches are not allowed: $branch"
            }

            if ($allBranches -notcontains $branch) {
                throw "Unsupported branch: $branch"
            }
        }

        return $customBranches
    }

    $normalizedProfile = $RequestedProfile.Trim().ToLowerInvariant()
    switch ($normalizedProfile) {
        'all' {
            return $allBranches
        }
        'forge' {
            return $activeBranchGroups.forge
        }
        'fabric' {
            return $activeBranchGroups.fabric
        }
        'neoforge' {
            return $activeBranchGroups.neoforge
        }
        default {
            throw "Unsupported profile: $RequestedProfile"
        }
    }
}

function Resolve-JavaVersion {
    param(
        [string]$Version,
        [string]$GradlePropertiesPath,
        [string]$BuildGradlePath
    )

    $buildContent = ''
    $requestedJavaVersion = $null

    if (Test-Path $GradlePropertiesPath) {
        $javaVersionLine = Get-Content $GradlePropertiesPath | Where-Object { $_ -match '^\s*java_version\s*=\s*(\d+)\s*$' } | Select-Object -First 1
        if ($javaVersionLine -match '(\d+)') {
            $requestedJavaVersion = [int]$Matches[1]
        }
    }

    if (Test-Path $BuildGradlePath) {
        $buildContent = Get-Content $BuildGradlePath -Raw
        if (-not $requestedJavaVersion -and $buildContent -match 'JavaLanguageVersion\.of\((\d+)\)') {
            $requestedJavaVersion = [int]$Matches[1]
        }
    }

    if (-not $requestedJavaVersion) {
        $requestedJavaVersion = switch -Regex ($Version) {
            '^1\.(17\.1|18\.2|19\.2|19\.4|20\.1)$' { 17; break }
            '^1\.(20\.6|21\.1|21\.5|21\.11)$' { 21; break }
            default { 8 }
        }
    }

    if ($buildContent -match "id\s+['""]fabric-loom['""]") {
        return [Math]::Max(17, $requestedJavaVersion)
    }

    return $requestedJavaVersion
}

function Get-RepositoryUrl {
    if (
        -not [string]::IsNullOrWhiteSpace($env:GITHUB_TOKEN) -and
        -not [string]::IsNullOrWhiteSpace($env:GITHUB_REPOSITORY)
    ) {
        return "https://x-access-token:$($env:GITHUB_TOKEN)@github.com/$($env:GITHUB_REPOSITORY).git"
    }

    return (git remote get-url origin).Trim()
}

$excludedVersions = @('1.7.2', '1.7.10')
$repoUrl = Get-RepositoryUrl
$targetBranches = @(Get-TargetBranches -RequestedProfile $Profile -RequestedBranches $Branches)
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) 'EasyLAN-aggregate-branches'

if (Test-Path $tempRoot) {
    Remove-Item -LiteralPath $tempRoot -Recurse -Force
}

New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null

try {
    $matrixInclude = New-Object System.Collections.Generic.List[object]

    foreach ($branch in $targetBranches) {
        $branchPath = Join-Path $tempRoot ($branch -replace '[^A-Za-z0-9._-]', '_')
        & git clone --depth 1 --single-branch --branch $branch $repoUrl $branchPath | Out-Null

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to clone branch: $branch"
        }

        $versionsRoot = Join-Path $branchPath 'versions'
        if (-not (Test-Path $versionsRoot)) {
            continue
        }

        Get-ChildItem -Path $versionsRoot -Directory |
            Sort-Object Name |
            Where-Object {
                $projectPath = Join-Path $_.FullName 'project'
                (Test-Path (Join-Path $projectPath 'gradlew.bat')) -and
                ($excludedVersions -notcontains $_.Name)
            } |
            ForEach-Object {
                $projectPath = Join-Path $_.FullName 'project'
                $javaVersion = Resolve-JavaVersion `
                    -Version $_.Name `
                    -GradlePropertiesPath (Join-Path $projectPath 'gradle.properties') `
                    -BuildGradlePath (Join-Path $projectPath 'build.gradle')

                $matrixInclude.Add([PSCustomObject]@{
                    branch = $branch
                    version = $_.Name
                    java = [string]$javaVersion
                }) | Out-Null
            }
    }

    $matrixItems = @($matrixInclude.ToArray())

    [PSCustomObject]@{
        include = $matrixItems
    } | ConvertTo-Json -Compress -Depth 5
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
