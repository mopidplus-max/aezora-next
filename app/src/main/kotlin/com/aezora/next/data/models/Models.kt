package com.aezora.next.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Junction

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String = "",
    val streamUrl: String = "",
    val duration: Long = 0L,
    val service: String = MusicService.LOCAL.name,
    val serviceId: String = "",
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val waveformUrl: String? = null,
    val genre: String? = null,
    val playCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val artworkUrl: String = "",
    val service: String = MusicService.LOCAL.name,
    val serviceId: String = "",
    val ownerName: String = "",
    val trackCount: Int = 0,
    val isImported: Boolean = false,
    val isLocal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrack(
    val playlistId: String,
    val trackId: String,
    val position: Int = 0
)

data class PlaylistWithTracks(
    @Embedded
    val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrack::class,
            parentColumn = "playlistId",
            entityColumn = "trackId"
        )
    )
    val tracks: List<Track> = emptyList()
)

@Entity(tableName = "service_accounts")
data class ServiceAccount(
    @PrimaryKey
    val service: String,
    val accessToken: String = "",
    val refreshToken: String = "",
    val isConnected: Boolean = false,
    val displayName: String = ""
)

enum class MusicService(val displayName: String, val logoUrl: String = "") {
    SOUNDCLOUD("SoundCloud"),
    VK("VK"),
    YANDEX("Яндекс Музыка"),
    YOUTUBE("YouTube"),
    LOCAL("Локальная")
}

enum class PlaybackSpeed(val value: Float, val pitchSemitones: Int, val label: String) {
    SLOWED(0.9f, -2, "0.9x"),
    DEFAULT(1.0f, 0, "1.0x"),
    SPEEDUP(1.1f, 2, "1.1x")
}

data class QueueItem(
    val track: Track
)

data class SoundCloudSearchResult(
    val collection: List<SoundCloudTrack> = emptyList()
)

data class SoundCloudTrack(
    val id: Long = 0,
    val title: String = "",
    val user: SoundCloudUser = SoundCloudUser(),
    val artwork_url: String? = null,
    val stream_url: String? = null,
    val duration: Long = 0L,
    val media: SoundCloudMedia? = null,
    val waveform_url: String? = null,
    val genre: String? = null,
    val playback_count: Int = 0
)

data class SoundCloudPlaylist(
    val id: Long = 0,
    val title: String = "",
    val artwork_url: String? = null,
    val user: SoundCloudUser = SoundCloudUser(),
    val track_count: Int = 0
)

data class SoundCloudUser(
    val username: String = ""
)

data class SoundCloudMedia(
    val transcodings: List<SoundCloudTranscoding> = emptyList()
)

data class SoundCloudTranscoding(
    val format: SoundCloudFormat = SoundCloudFormat()
)

data class SoundCloudFormat(
    val protocol: String = ""
)

data class VKTrack(
    val owner_id: Long = 0,
    val id: Long = 0,
    val title: String = "",
    val artist: String = "",
    val duration: Int = 0,
    val url: String = "",
    val album: VKAlbum? = null
)

data class VKAlbum(
    val thumb: VKThumb? = null
)

data class VKThumb(
    val photo_300: String = ""
)

data class VKPlaylist(
    val owner_id: Long = 0,
    val id: Long = 0,
    val title: String = "",
    val photo: VKPhoto? = null,
    val count: Int = 0
)

data class VKPhoto(
    val photo_300: String = ""
)

data class YandexPlaylist(
    val uid: Long = 0,
    val kind: Long = 0,
    val title: String = "",
    val trackCount: Int = 0,
    val cover: YandexCover? = null
)

data class YandexCover(
    val uri: String = ""
)

data class YandexTrack(
    val id: Long = 0,
    val title: String = "",
    val artists: List<YandexArtist> = emptyList(),
    val albums: List<YandexAlbum> = emptyList(),
    val durationMs: Long = 0L
)

data class YandexArtist(
    val name: String = ""
)

data class YandexAlbum(
    val title: String = "",
    val coverUri: String? = null
)

data class StreamUrlResponse(
    val url: String? = null,
    val http_mp3_128_url: String? = null
)
