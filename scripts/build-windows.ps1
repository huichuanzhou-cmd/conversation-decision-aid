$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Resolve-Path (Join-Path $PSScriptRoot '..'))

New-Item -ItemType Directory -Force -Path 'build/classes', 'build/test-classes', 'build/package-input', 'dist' | Out-Null

$mainSources = @(Get-ChildItem -LiteralPath 'src/main/java' -Filter '*.java' | ForEach-Object { $_.FullName })
javac -encoding UTF-8 -d build/classes $mainSources
if ($LASTEXITCODE -ne 0) { throw 'Main source compilation failed.' }
Copy-Item -LiteralPath (Get-ChildItem -LiteralPath 'src/main/resources' -Filter '*.properties' | ForEach-Object { $_.FullName }) -Destination 'build/classes'

$testSources = @(Get-ChildItem -LiteralPath 'src/test/java' -Filter '*.java' | ForEach-Object { $_.FullName })
javac -encoding UTF-8 -cp build/classes -d build/test-classes $testSources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }

java -cp 'build/classes;build/test-classes' CalculationTest
if ($LASTEXITCODE -ne 0) { throw 'Calculation tests failed.' }
java -cp 'build/classes;build/test-classes' LocalizationTest
if ($LASTEXITCODE -ne 0) { throw 'Localization tests failed.' }

jar --create --file build/package-input/ConversationDecisionAid.jar --main-class ConversationDecisionAid -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed.' }

$distRoot = [System.IO.Path]::GetFullPath((Join-Path (Get-Location).Path 'dist'))
$appImage = [System.IO.Path]::GetFullPath((Join-Path $distRoot 'ConversationDecisionAid'))
if ([System.IO.Path]::GetDirectoryName($appImage) -ne $distRoot) {
    throw 'Unexpected application image path.'
}
if (Test-Path -LiteralPath $appImage) {
    Remove-Item -LiteralPath $appImage -Recurse -Force
}

jpackage --type app-image --name ConversationDecisionAid --app-version 1.0.0 --input build/package-input --main-jar ConversationDecisionAid.jar --main-class ConversationDecisionAid --dest dist
if ($LASTEXITCODE -ne 0) { throw 'Windows application image creation failed.' }

Compress-Archive -LiteralPath 'dist/ConversationDecisionAid' -DestinationPath 'dist/ConversationDecisionAid-v1.0.0-Windows-x64.zip' -Force
Write-Output 'Created dist/ConversationDecisionAid-v1.0.0-Windows-x64.zip'
