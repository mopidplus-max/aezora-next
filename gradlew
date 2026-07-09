#!/bin/bash

set -e

GRADLE_VERSION="8.4"
GRADLE_HOME="$HOME/.gradle/wrapper/gradle-$GRADLE_VERSION"

# Скачать gradle если нужно
if [ ! -f "$GRADLE_HOME/bin/gradle" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$HOME/.gradle/wrapper"
    TEMP_DIR=$(mktemp -d)
    cd "$TEMP_DIR"
    curl -sL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "gradle-$GRADLE_VERSION-bin.zip"
    unzip -q "gradle-$GRADLE_VERSION-bin.zip"
    mkdir -p "$(dirname "$GRADLE_HOME")"
    mv "gradle-$GRADLE_VERSION" "$GRADLE_HOME"
    rm -rf "$TEMP_DIR"
fi

# Запустить gradle в текущей директории
cd "$(dirname "$0")"
exec "$GRADLE_HOME/bin/gradle" "$@"
