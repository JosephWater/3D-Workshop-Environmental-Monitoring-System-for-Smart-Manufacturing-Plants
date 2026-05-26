$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$pythonScript = Join-Path $scriptDir "create_workshop_model.py"

$candidatePaths = @()

$candidatePaths += "E:\PluginsAndTool\Blender\blender.exe"

$pathCommand = Get-Command blender -ErrorAction SilentlyContinue
if ($pathCommand) {
    $candidatePaths += $pathCommand.Source
}

$foundInstallPaths = Get-ChildItem `
    -Path "C:\Program Files\Blender Foundation", "C:\Program Files", "C:\Program Files (x86)" `
    -Filter "blender.exe" `
    -Recurse `
    -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty FullName

$candidatePaths += $foundInstallPaths

$blender = $candidatePaths |
    Where-Object { $_ -and (Test-Path $_) } |
    Select-Object -First 1

if (-not $blender) {
    Write-Error "Blender was not found. Install Blender from https://www.blender.org/download/ and rerun this script."
}

Write-Host "Using Blender: $blender"
Write-Host "Generating workshop model..."

& $blender --background --python $pythonScript

$output = Join-Path $repoRoot "frontend\public\models\workshop.glb"
if (-not (Test-Path $output)) {
    Write-Error "Model generation finished, but workshop.glb was not found at $output"
}

Write-Host "Generated: $output"
