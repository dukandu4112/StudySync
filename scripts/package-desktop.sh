#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn --batch-mode -Pdesktop-qa clean verify
mkdir -p target/desktop-input
cp target/studysync-3.0.0-rc.4-all.jar target/desktop-input/
jpackage --type app-image --name StudySync --app-version 3.0.0 --input target/desktop-input --main-jar studysync-3.0.0-rc.4-all.jar --main-class com.studysync.DesktopLauncher --dest target/desktop --vendor "Daniel Ukandu"
