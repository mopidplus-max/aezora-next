# Aezora Next 🎵

Полнофункциональный музыкальный плеер для Android, написанный на Kotlin + Jetpack Compose.

## Возможности

- 🎨 **3 темы**: Тёмно-белая, Жёлто-зелёная, Сине-фиолетовая
- 🎵 **SoundCloud** — встроенный API (поиск, тренды, плейлисты)
- 🔗 **Импорт плейлистов** из VK, Яндекс Музыки, SoundCloud
- 🔐 **Web-вход** через OAuth2 WebView для каждого сервиса
- 🔑 **Ручной ввод токена** (OAuth / Bearer)
- 🐌 **Slowed** (−2 тона) / ⚡ **SpeedUp** (+2 тона) — pitch shifting через ExoPlayer
- 💿 Анимированный **виниловый диск** в плеере
- 📋 **Очередь** треков с drag-and-drop
- ❤️ **Избранное** (локальная БД через Room)
- 🔀 Shuffle, Repeat (Off / All / One)
- 🔊 Управление громкостью
- 📱 **Mini Player** с прогресс-баром
- 🔔 Уведомления с MediaSession (управление из шторки)

## Структура проекта

```
app/src/main/kotlin/com/aezora/next/
├── AezoraApp.kt                    # Application class
├── MainActivity.kt                 # Точка входа
├── data/
│   ├── api/
│   │   ├── SoundCloudApi.kt        # SC API v1
│   │   ├── SoundCloudApiV2.kt      # SC API v2 (треки, плейлисты)
│   │   ├── VKApi.kt                # VK Music API
│   │   ├── YandexMusicApi.kt       # Яндекс Музыка API
│   │   └── NetworkClient.kt        # Retrofit клиенты
│   ├── db/
│   │   └── AezoraDatabase.kt       # Room DB + DAO
│   ├── models/
│   │   └── Models.kt               # Track, Playlist, ServiceAccount и т.д.
│   └── repository/
│       └── MusicRepository.kt      # Единый репозиторий данных
├── service/
│   └── AezoraPlaybackService.kt    # MediaSessionService (ExoPlayer)
└── ui/
    ├── Navigation.kt               # NavHost + BottomBar
    ├── components/
    │   └── MiniPlayer.kt           # Мини-плеер внизу экрана
    ├── screens/
    │   ├── home/
    │   │   ├── HomeViewModel.kt
    │   │   └── HomeScreen.kt       # Поиск + тренды
    │   ├── library/
    │   │   ├── LibraryViewModel.kt
    │   │   └── LibraryScreen.kt    # Плейлисты + избранное
    │   ├── player/
    │   │   ├── PlayerViewModel.kt  # Состояние плеера
    │   │   └── PlayerScreen.kt     # Полноэкранный плеер (винил)
    │   ├── auth/
    │   │   └── ServicesScreen.kt   # Вход в сервисы (Web / токен)
    │   └── settings/
    │       └── SettingsScreen.kt   # Настройки, темы, качество
    └── theme/
        ├── Color.kt                # 3 цветовые схемы
        ├── Type.kt                 # Типографика
        └── Theme.kt                # AezoraNextTheme
```

## Сборка

1. Открыть в **Android Studio Hedgehog** или новее
2. Синхронизировать Gradle
3. Запустить на устройстве / эмуляторе (API 26+)

## Сервисы и авторизация

| Сервис | Метод входа |
|---|---|
| SoundCloud | OAuth2 WebView или токен вручную |
| VK Music | OAuth2 WebView (mobile authorize) |
| Яндекс Музыка | OAuth2 WebView или токен вручную |

### Импорт плейлистов

В разделе **Библиотека → кнопка импорта** вставьте ссылку:
- `https://soundcloud.com/user/sets/playlist-name`
- `https://vk.com/music/playlist/-123456_789`
- `https://music.yandex.ru/users/user/playlists/123`

## Slowed / SpeedUp

| Режим | Скорость | Сдвиг |
|---|---|---|
| Slowed | ×0.85 | −2 полутона |
| Default | ×1.00 | 0 |
| SpeedUp | ×1.15 | +2 полутона |

Реализовано через `PlaybackParameters` ExoPlayer с pitch factor `2^(semitones/12)`.

## Зависимости

- Jetpack Compose BOM 2024.02
- Media3 ExoPlayer 1.2.1
- Retrofit 2.9.0
- Room 2.6.1
- Coil 2.5.0
- Accompanist 0.32.0
