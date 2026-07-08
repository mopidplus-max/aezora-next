package com.aezora.next.data.models

import androidx.room.*

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class MusicService(val displayName: String, val logoUrl: String) {
    SOUNDCLOUD("SoundCloud", "https://developers.soundcloud.com/assets/logo_white-af5006050dd9cba09b0c48be04feac57.png"),
    YOUTUBE   ("YouTube Music", "https://music.youtube.com/img/favicon_144.png"),
    VK        ("VK Music", "https://vk.com/images/icons/favicons/fav_logo.ico"),
    YANDEX    ("Яндекс Музыка", "https://music.yandex.ru/blocks/meta/i/yandex-music-logo-2x.png"),
    LOCAL     ("Локально", "")
}

enum class PlaybackSpeed(val label: String, val value: Float, val pitchSemitones: Int) {
    SLOWED ("Slowed",  0.85f, -2),
    DEFAULT("Default", 1.00f,  0),
    SPEEDUP("Speedup", 1.15f, +2)
}

// ── Room Entities ─────────────────────────────────────────────────────────────

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String = "",
    val streamUrl: String = "",
    val duration: Long = 0L,          // ms
    val service: String = MusicService.SOUNDCLOUD.name,
    val serviceId: String = "",       // native ID on that service
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String = "",
    val waveformUrl: String = "",
    val genre: String = "",
    val playCount: Long = 0L,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val artworkUrl: String = "",
    val service: String = MusicService.LOCAL.name,
    val serviceId: String = "",
    val ownerName: String = "",
    val trackCount: Int = 0,
    val isImported: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrack(
    val playlistId: String,
    val trackId: String,
    val position: Int = 0
)

@Entity(tableName = "service_accounts")
data class ServiceAccount(
    @PrimaryKey val service: String,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAt: Long = 0L,
    val isConnected: Boolean = false
)

// ── Relations ─────────────────────────────────────────────────────────────────

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "trackId",
        associateBy = Junction(PlaylistTrack::class, parentColumn = "playlistId", entityColumn = "trackId")
    )
    val tracks: List<Track>
)

// ── API response models ───────────────────────────────────────────────────────

data class SoundCloudTrack(
    val id: Long = 0,
    val title: String = "",
    val user: SoundCloudUser = SoundCloudUser(),
    val artwork_url: String? = null,
    val stream_url: String? = null,
    val duration: Long = 0,
    val waveform_url: String? = null,
    val genre: String? = null,
    val playback_count: Long = 0,
    val media: SoundCloudMedia? = null
)

data class SoundCloudUser(val id: Long = 0, val username: String = "")

data class SoundCloudMedia(val transcodings: List<SoundCloudTranscoding> = emptyList())

data class SoundCloudTranscoding(
    val url: String = "",
    val format: SoundCloudFormat = SoundCloudFormat()
)

data class SoundCloudFormat(val protocol: String = "", val mime_type: String = "")

data class SoundCloudPlaylist(
    val id: Long = 0,
    val title: String = "",
    val user: SoundCloudUser = SoundCloudUser(),
    val artwork_url: String? = null,
    val track_count: Int = 0,
    val tracks: List<SoundCloudTrack> = emptyList()
)

data class SoundCloudSearchResult(
    val collection: List<SoundCloudTrack> = emptyList(),
    val total_results: Int = 0
)

data class StreamUrlResponse(val url: String = "")

// VK Models
data class VKTrack(
    val id: Long = 0,
    val owner_id: Long = 0,
    val artist: String = "",
    val title: String = "",
    val duration: Int = 0,
    val url: String = "",
    val album: VKAlbum? = null
)

data class VKAlbum(val id: Long = 0, val title: String = "", val thumb: VKThumb? = null)
data class VKThumb(val photo_300: String = "")

data class VKPlaylist(
    val id: Long = 0,
    val owner_id: Long = 0,
    val title: String = "",
    val description: String = "",
    val count: Int = 0,
    val photo: VKThumb? = null
)

// Yandex Music Models
data class YandexTrack(
    val id: Long = 0,
    val title: String = "",
    val artists: List<YandexArtist> = emptyList(),
    val albums: List<YandexAlbum> = emptyList(),
    val durationMs: Long = 0
)

data class YandexArtist(val id: Long = 0, val name: String = "")
data class YandexAlbum(val id: Long = 0, val title: String = "", val coverUri: String? = null)

data class YandexPlaylist(
    val uid: Long = 0,
    val kind: Long = 0,
    val title: String = "",
    val trackCount: Int = 0,
    val cover: YandexCover? = null
)

data class YandexCover(val uri: String? = null)

// Player queue model
data class QueueItem(
    val track: Track,
    val queueId: String = java.util.UUID.randomUUID().toString()
)
