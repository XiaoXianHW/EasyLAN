param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectDirectory,
    [Parameter(Mandatory = $true)]
    [string]$Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $ProjectDirectory)) {
    throw "Project directory not found: $ProjectDirectory"
}

if ([string]::IsNullOrWhiteSpace($Version)) {
    throw 'Version must not be empty.'
}

function Update-VersionInFile {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Replacement
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return $false
    }

    $bytes = [System.IO.File]::ReadAllBytes($Path)
    $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF
    $encoding = New-Object System.Text.UTF8Encoding($hasBom)
    $content = $encoding.GetString($bytes)
    if ($hasBom) {
        $content = $content.Substring(1)
    }

    $updated = [System.Text.RegularExpressions.Regex]::Replace($content, $Pattern, $Replacement)
    if ($updated -ceq $content) {
        return $false
    }

    # Only the matched line changes, line endings and the trailing newline stay as they were.
    [System.IO.File]::WriteAllText($Path, $updated, $encoding)
    return $true
}

$patched = @()

$gradlePropertiesPatched = Update-VersionInFile `
    -Path (Join-Path $ProjectDirectory 'gradle.properties') `
    -Pattern '(?m)^([ \t]*mod_version[ \t]*=[ \t]*)[^\r\n]*$' `
    -Replacement "`${1}$Version"

if ($gradlePropertiesPatched) {
    $patched += 'gradle.properties'
}

# Only the top level project version assignment starts at column zero, nested ones
# (for example the forge dependency version) are indented and must stay untouched.
$buildGradlePatched = Update-VersionInFile `
    -Path (Join-Path $ProjectDirectory 'build.gradle') `
    -Pattern '(?m)^version[ \t]*=[ \t]*[''"][^''"\r\n]*[''"][ \t]*$' `
    -Replacement "version = '$Version'"

if ($buildGradlePatched) {
    $patched += 'build.gradle'
}

if ($patched.Count -gt 0) {
    Write-Host "Applied mod version $Version in $ProjectDirectory via $($patched -join ', ')"
    return
}

$declaresModVersion = (Test-Path -LiteralPath (Join-Path $ProjectDirectory 'gradle.properties')) -and
    ((Get-Content -LiteralPath (Join-Path $ProjectDirectory 'gradle.properties') -Raw) -match '(?m)^[ \t]*mod_version[ \t]*=')
$declaresLiteralVersion = (Test-Path -LiteralPath (Join-Path $ProjectDirectory 'build.gradle')) -and
    ((Get-Content -LiteralPath (Join-Path $ProjectDirectory 'build.gradle') -Raw) -match '(?m)^version[ \t]*=[ \t]*[''"]')

if ($declaresModVersion -or $declaresLiteralVersion) {
    Write-Host "Mod version in $ProjectDirectory is already $Version"
    return
}

throw "Unable to apply mod version in $ProjectDirectory (no mod_version property and no literal top level version assignment)."
