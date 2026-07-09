#!/bin/bash

echo "🔧 Исправляю Gradle..."

cd ~/AezoraNext

# Очистка
echo "1️⃣ Очищаю кэш..."
./gradlew clean
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Создать резервную копию
echo "2️⃣ Создаю резервную копию..."
cp app/build.gradle app/build.gradle.backup

# Замены
echo "3️⃣ Применяю исправления..."
sed -i "s/kotlinCompilerExtensionVersion '1\.5\.8'/kotlinCompilerExtensionVersion '1.5.14'/g" app/build.gradle
sed -i "s/compose-bom:2024\.02\.00/compose-bom:2024.06.00/g" app/build.gradle
sed -i "s/androidx\.media3:media3-exoplayer:1\.2\.1/androidx.media3:media3-exoplayer:1.3.1/g" app/build.gradle
sed -i "s/androidx\.media3:media3-ui:1\.2\.1/androidx.media3:media3-ui:1.3.1/g" app/build.gradle
sed -i "s/androidx\.media3:media3-session:1\.2\.1/androidx.media3:media3-session:1.3.1/g" app/build.gradle
sed -i "s/androidx\.media3:media3-common:1\.2\.1/androidx.media3:media3-common:1.3.1/g" app/build.gradle

# Проверка
echo "4️⃣ Проверяю изменения..."
echo ""
echo "✅ Обновлено:"
grep -n "1\.5\.14\|2024\.06\.00\|1\.3\.1" app/build.gradle
echo ""

# Сборка
echo "5️⃣ Пытаюсь собрать проект..."
./gradlew assembleRelease --stacktrace

echo ""
echo "✨ Готово!"
