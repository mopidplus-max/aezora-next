package com.aezora.next.data.models

import androidx.room.*

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val streamUrl: String,
    val duration: Long,
    val service: String,
    val serviceId: String,
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
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val isLocal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlaylistWithTracks(
    @Embedded
    val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val tracks: List<Track> = emptyList()
)

@Entity(
    tableName = "playlist_track_cross_ref",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String
)
