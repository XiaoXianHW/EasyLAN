Set-StrictMode -Version Latest

function Get-OptionalSharedProperty {
    param(
        [Parameter(Mandatory = $true)]
        $Object,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        $DefaultValue = $null
    )

    if ($Object.PSObject.Properties.Name -contains $Name) {
        return $Object.$Name
    }

    return $DefaultValue
}

function Get-SharedSyncMap {
    param(
        [Parameter(Mandatory = $true)]
        [string]$MapPath
    )

    if (-not (Test-Path $MapPath)) {
        throw "Shared sync map does not exist: $MapPath"
    }

    return Get-Content $MapPath -Raw | ConvertFrom-Json
}

function Get-ActiveSharedBranches {
    param(
        [Parameter(Mandatory = $true)]
        $Map
    )

    $branches = @()

    foreach ($source in @($Map.sources)) {
        foreach ($target in @($source.targets)) {
            if ($branches -notcontains $target.branch) {
                $branches += $target.branch
            }
        }
    }

    return $branches
}

function Resolve-SharedBranches {
    param(
        [Parameter(Mandatory = $true)]
        $Map,
        [string]$Profile = 'all',
        [string]$Branches = ''
    )

    $availableBranches = @(Get-ActiveSharedBranches -Map $Map)

    if (-not [string]::IsNullOrWhiteSpace($Branches)) {
        $customBranches = @(
            $Branches -split '[,\s]+' |
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

            if ($availableBranches -notcontains $branch) {
                throw "Unsupported branch: $branch"
            }
        }

        return $customBranches
    }

    $normalizedProfile = $Profile.Trim().ToLowerInvariant()
    switch ($normalizedProfile) {
        'all' {
            return $availableBranches
        }
        'forge' {
            return @($availableBranches | Where-Object { $_ -like 'forge-*' })
        }
        'fabric' {
            return @($availableBranches | Where-Object { $_ -like 'fabric-*' })
        }
        'neoforge' {
            return @($availableBranches | Where-Object { $_ -like 'neoforge-*' })
        }
        default {
            throw "Unsupported profile: $Profile"
        }
    }
}

function Get-BranchSharedTargets {
    param(
        [Parameter(Mandatory = $true)]
        $Map,
        [Parameter(Mandatory = $true)]
        [string]$Branch
    )

    $result = @()

    foreach ($source in @($Map.sources)) {
        foreach ($target in @($source.targets)) {
            if ($target.branch -eq $Branch) {
                $versions = @(Get-OptionalSharedProperty -Object $target -Name 'versions' -DefaultValue @())

                if ($versions.Count -gt 0) {
                    $sourceTemplate = Get-OptionalSharedProperty -Object $target -Name 'sourceTemplate' -DefaultValue ''
                    $destinationTemplate = Get-OptionalSharedProperty -Object $target -Name 'destinationTemplate' -DefaultValue ''

                    if ([string]::IsNullOrWhiteSpace($sourceTemplate)) {
                        throw "Missing sourceTemplate for $Branch in source $($source.name)"
                    }

                    if ([string]::IsNullOrWhiteSpace($destinationTemplate)) {
                        throw "Missing destinationTemplate for $Branch in source $($source.name)"
                    }

                    foreach ($version in $versions) {
                        $resolvedSource = $sourceTemplate.Replace('{{version}}', $version)
                        $resolvedDestination = $destinationTemplate.Replace('{{version}}', $version)

                        $result += [PSCustomObject]@{
                            name = "$($source.name)-$version"
                            source = $resolvedSource
                            destination = $resolvedDestination
                            branch = $Branch
                            version = $version
                        }
                    }

                    continue
                }

                $sourcePath = Get-OptionalSharedProperty -Object $source -Name 'source' -DefaultValue ''
                $destinationPath = Get-OptionalSharedProperty -Object $target -Name 'destination' -DefaultValue ''

                $result += [PSCustomObject]@{
                    name = $source.name
                    source = $sourcePath
                    destination = $destinationPath
                    branch = $Branch
                }
            }
        }
    }

    return $result
}

function Sync-DirectoryContents {
    param(
        [Parameter(Mandatory = $true)]
        [string]$SourcePath,
        [Parameter(Mandatory = $true)]
        [string]$DestinationPath
    )

    if (-not (Test-Path $SourcePath)) {
        throw "Shared source does not exist: $SourcePath"
    }

    $destinationParent = Split-Path $DestinationPath -Parent
    if (-not [string]::IsNullOrWhiteSpace($destinationParent) -and -not (Test-Path $destinationParent)) {
        New-Item -ItemType Directory -Path $destinationParent -Force | Out-Null
    }

    if (Test-Path $DestinationPath) {
        Remove-Item -LiteralPath $DestinationPath -Recurse -Force
    }

    New-Item -ItemType Directory -Path $DestinationPath -Force | Out-Null

    foreach ($item in Get-ChildItem -LiteralPath $SourcePath -Force) {
        Copy-Item -LiteralPath $item.FullName -Destination $DestinationPath -Recurse -Force
    }
}
