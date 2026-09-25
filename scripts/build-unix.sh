#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
platform="${1:-}"
if [[ "$platform" != "macos" && "$platform" != "linux" ]]; then
  echo 'Usage: bash scripts/build-unix.sh macos|linux' >&2
  exit 1
fi

mkdir -p build/classes build/test-classes build/package-input dist
javac -encoding UTF-8 -d build/classes src/main/java/*.java
cp src/main/resources/*.properties build/classes/
javac -encoding UTF-8 -cp build/classes -d build/test-classes src/test/java/*.java
java -cp build/classes:build/test-classes CalculationTest
java -cp build/classes:build/test-classes LocalizationTest
jar --create --file build/package-input/ConversationDecisionAid.jar --main-class ConversationDecisionAid -C build/classes .

if [[ "$platform" == "macos" ]]; then
  jpackage --type dmg --name ConversationDecisionAid --app-version 0.3.1 \
    --input build/package-input --main-jar ConversationDecisionAid.jar \
    --main-class ConversationDecisionAid --dest dist
  packages=(dist/*.dmg)
  mv "${packages[0]}" dist/ConversationDecisionAid-v0.3.1-macOS.dmg
  echo 'Created dist/ConversationDecisionAid-v0.3.1-macOS.dmg'
else
  jpackage --type deb --name ConversationDecisionAid --app-version 0.3.1 \
    --input build/package-input --main-jar ConversationDecisionAid.jar \
    --main-class ConversationDecisionAid --dest dist \
    --linux-package-name conversation-decision-aid --linux-shortcut
  packages=(dist/*.deb)
  mv "${packages[0]}" dist/ConversationDecisionAid-v0.3.1-Linux.deb
  echo 'Created dist/ConversationDecisionAid-v0.3.1-Linux.deb'
fi
