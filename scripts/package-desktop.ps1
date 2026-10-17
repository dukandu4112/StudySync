$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
mvn --batch-mode -Pdesktop-qa clean verify
if ($LASTEXITCODE -ne 0) { throw "Build failed" }
New-Item -ItemType Directory -Force target/desktop-input | Out-Null
Copy-Item target/studysync-3.0.0-rc.4-all.jar target/desktop-input/
jpackage --type app-image --name StudySync --app-version 3.0.0 --input target/desktop-input --main-jar studysync-3.0.0-rc.4-all.jar --main-class com.studysync.DesktopLauncher --dest target/desktop --vendor "Daniel Ukandu"
if ($LASTEXITCODE -ne 0) { throw "Desktop packaging failed" }
