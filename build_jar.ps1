Param(
    [string]$JarName = "AidenAdventure.jar"
)

Write-Host "Building runnable JAR: $JarName"

$ErrorActionPreference = 'Stop'

# Paths
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$OutDir = Join-Path $ProjectRoot 'out'
$SrcDir = Join-Path $ProjectRoot 'src'
$ResDir = Join-Path $ProjectRoot 'res'
$ManifestPath = Join-Path $OutDir 'MANIFEST.MF'

# Clean out dir
if (Test-Path $OutDir) {
    Remove-Item -Recurse -Force $OutDir
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# Compile sources
$javaFiles = Get-ChildItem -Path $SrcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if ($javaFiles.Count -eq 0) {
    throw "No .java files found under '$SrcDir'"
}

Write-Host "Compiling Java sources..."
javac -encoding UTF-8 -d $OutDir -sourcepath $SrcDir @javaFiles

# Copy resources into classpath root inside out/
if (Test-Path $ResDir) {
    Write-Host "Copying resources..."
    Copy-Item -Recurse -Force $ResDir $OutDir
}

# Create manifest with the correct Main-Class (package src.main.Main)
$manifest = @(
    'Manifest-Version: 1.0',
    'Main-Class: src.main.Main'
) -join "`r`n"
Set-Content -Path $ManifestPath -Value ($manifest + "`r`n") -Encoding Ascii

# Create jar
if (Test-Path $JarName) { Remove-Item -Force $JarName }

Write-Host "Packaging JAR..."
jar cfm $JarName $ManifestPath -C $OutDir .

Write-Host "Done."
Write-Host "Run with: java -jar `"$JarName`""
