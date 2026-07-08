#!/bin/bash

echo "🔐 Создание Keystore для подписи APK"
echo ""

read -p "Введите пароль для keystore: " -s KEYSTORE_PASSWORD
echo ""
read -p "Повторите пароль: " -s KEYSTORE_PASSWORD_CONFIRM
echo ""

if [ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_CONFIRM" ]; then
    echo "❌ Пароли не совпадают"
    exit 1
fi

read -p "Введите пароль для ключа: " -s KEY_PASSWORD
echo ""
read -p "Введите alias ключа (например: aezora_key): " KEY_ALIAS
echo ""
read -p "Введите ваше имя и фамилию: " FIRSTNAME_LASTNAME
read -p "Введите название организации: " ORG_NAME
read -p "Введите город: " CITY
read -p "Введите регион/штат: " STATE
read -p "Введите код страны (RU, US и т.д.): " COUNTRY

# Создаем keystore
keytool -genkey -v -keystore aezora.jks \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias "$KEY_ALIAS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$FIRSTNAME_LASTNAME, O=$ORG_NAME, L=$CITY, ST=$STATE, C=$COUNTRY"

echo ""
echo "✅ Keystore создан: aezora.jks"
echo ""
echo "📋 Теперь кодируем в base64 для GitHub Secrets:"
echo ""

# Кодируем в base64
KEYSTORE_ENCODED=$(base64 -i aezora.jks)

echo "📝 Сохраняем в файл keystore-base64.txt..."
echo "$KEYSTORE_ENCODED" > keystore-base64.txt

echo ""
echo "✅ Готово!"
echo ""
echo "🔐 Добавьте эти секреты в GitHub (Settings → Secrets):"
echo ""
echo "KEYSTORE_ENCODED:"
echo "$(head -c 50 keystore-base64.txt)..."
echo ""
echo "KEYSTORE_PASSWORD:"
echo "$KEYSTORE_PASSWORD"
echo ""
echo "KEY_ALIAS:"
echo "$KEY_ALIAS"
echo ""
echo "KEY_PASSWORD:"
echo "$KEY_PASSWORD"
echo ""
echo "⚠️  Не загружайте файлы aezora.jks и keystore-base64.txt в GitHub!"
echo "    Они уже добавлены в .gitignore"
