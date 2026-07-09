# Aezora Next 🎵

Музыкальный плеер для Android (Kotlin + Jetpack Compose).

## Что исправлено в этой версии

| Проблема | Решение |
|---|---|
| Треки SC не играли | `resolveSoundCloudStreamUrl()` — lazy резолвинг MP3 URL через transcoding endpoint |
| Лайки не работали | `toggleLike()` теперь сначала сохраняет трек в Room DB, потом переключает флаг |
| WebView вход | Убран полностью — только поле токена |
| Перенос плейлистов | При подключении сервиса автоматически вызывается `syncServicePlaylists()` |
| Ошибки плеера | `onPlayerError` → Snackbar с сообщением, спиннер в кнопке Play |

## Сборка

1. Открыть в **Android Studio Hedgehog+**
2. Sync Gradle
3. Run на устройстве / эмуляторе (Android 8.0+, API 26)

## Залить на GitHub

```bash
# 1. Создать репо на github.com (например: username/AezoraNext)

# 2. Инициализировать и залить
git init
git add .
git commit -m "feat: initial Aezora Next release"
git branch -M main
git remote add origin https://github.com/ВАШ_USERNAME/AezoraNext.git
git push -u origin main
```

После пуша GitHub Actions (`/.github/workflows/build.yml`) автоматически соберёт подписанный APK.

### Секреты для GitHub Actions

Добавь в **Settings → Secrets → Actions**:

| Секрет | Значение |
|---|---|
| `KEYSTORE_ENCODED` | `cat aezora.jks \| base64` |
| `KEYSTORE_PASSWORD` | пароль хранилища |
| `KEY_ALIAS` | `aezora_key` |
| `KEY_PASSWORD` | пароль ключа |

Создать keystore:
```bash
keytool -genkey -v -keystore aezora.jks -alias aezora_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

## Авторизация по токену

### SoundCloud
1. Войти на soundcloud.com в браузере
2. F12 → Application → Cookies → `oauth_token`
3. Скопировать значение в приложение

### VK Музыка
Использовать Kate Mobile или официальный клиент:
`Настройки → Безопасность → Активные сессии → скопировать access_token`

### Яндекс Музыка
1. Перейти на `oauth.yandex.ru`
2. Авторизоваться и выдать токен

После ввода токена — плейлисты автоматически импортируются в Библиотеку.

## Как работает воспроизведение SC

```
playTrack(track)
  └── resolveSoundCloudStreamUrl(serviceId)
        └── GET api-v2.soundcloud.com/tracks/{id}?client_id=...
              └── .media.transcodings → progressive MP3 URL
                    └── GET {transcoding.url}?client_id=... → { "url": "https://..." }
                          └── ExoPlayer.setMediaItem(realUrl)
```

## Slowed / SpeedUp

```kotlin
// −2 тона: pitchFactor = 2^(-2/12) ≈ 0.891
// +2 тона: pitchFactor = 2^(+2/12) ≈ 1.122
val pitchFactor = 2.0.pow(semitones / 12.0).toFloat()
player.playbackParameters = PlaybackParameters(speed, pitchFactor)
```
