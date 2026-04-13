param(
    [string]$Task = 'build',
    [Alias('ExtraArgs')]
    [string[]]$GradleArgs,
    [switch]$SkipServerCacheCheck,
    [switch]$StopOnError
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projects = & (Join-Path $PSScriptRoot 'Get-VersionProjects.ps1')
$results = @()

foreach ($project in $projects) {
    try {
        & (Join-Path $PSScriptRoot 'Invoke-Version.ps1') -Version $project.Version -Task $Task -GradleArgs $GradleArgs -SkipServerCacheCheck:$SkipServerCacheCheck
        $results += [PSCustomObject]@{
            Version = $project.Version
            Task = $Task
            Status = 'Success'
        }
    } catch {
        $results += [PSCustomObject]@{
            Version = $project.Version
            Task = $Task
            Status = 'Failed'
        }
        if ($StopOnError) {
            throw
        }
    }
}

$results
