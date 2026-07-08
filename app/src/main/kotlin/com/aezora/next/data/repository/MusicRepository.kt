package com.aezora.next.data.repository

import com.aezora.next.BuildConfig
import com.aezora.next.data.api.*
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(private val db: AezoraDatabase) {

    private val scApi   = NetworkClient.soundCloudApi
    private val scApiV2 = NetworkClient.soundCloudApiV2
    private val vkApi   = NetworkClient.vkApi
    private val ynApi   = NetworkClient.yandexApi
    private val clientId = BuildConfig.SOUNDCLOUD_CLIENT_ID

    // ── SoundCloud ────────────────────────────────────────────────────────────

    suspend fun searchSoundCloud(query: String, offset: Int = 0): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.searchTracks(query, clientId, offset = offset)
                result.collection.map { it.toTrack() }
            } catch (e: Exception) { emptyList() }
        }

    suspend fun getSoundCloudTrending(): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getTrending(clientId = clientId)
                result.collection.map { it.track.toTrack() }
            } catch (e: Exception) { emptyList() }
        }

    suspend fun resolveSoundCloudStreamUrl(trackId: String): String =
        withContext(Dispatchers.IO) {
            try {
                // Try progressive stream first
                val track = scApiV2.getTrack(trackId.toLong(), clientId)
                val progressive = track.media?.transcodings
                    ?.firstOrNull { it.format.protocol == "progressive" }
                if (progressive != null) {
                    val streamResp = scApi.getStreamUrl(trackId.toLong(), clientId)
                    streamResp["http_mp3_128_url"] ?: streamResp.values.firstOrNull() ?: ""
                } else {
                    ""
                }
            } catch (e: Exception) { "" }
        }

    suspend fun getSoundCloudLikes(token: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getLikedTracks("OAuth $token", clientId)
                result.collection.map { it.toTrack() }
            } catch (e: Exception) { emptyList() }
        }

    suspend fun getSoundCloudPlaylists(token: String): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getMyPlaylists("OAuth $token", clientId)
                result.collection.map { it.toPlaylist() }
            } catch (e: Exception) { emptyList() }
        }

    // ── VK ────────────────────────────────────────────────────────────────────

    suspend fun searchVK(token: String, query: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.searchAudio(token, query)
                result.response?.items?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun getVKPlaylists(token: String, ownerId: Long): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.getPlaylists(token, ownerId)
                result.response?.items?.map { it.toPlaylist() } ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun getVKLibrary(token: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.getAudio(token)
                result.response?.items?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

    // ── Yandex ───────────────────────────────────────────────────────────────

    suspend fun searchYandex(token: String, query: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = ynApi.search(query, auth = "OAuth $token")
                result.result?.tracks?.results?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun getYandexPlaylists(token: String, uid: Long): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = ynApi.getUserPlaylists(uid, "OAuth $token")
                result.result?.map { it.toPlaylist() } ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

    // ── Local DB ──────────────────────────────────────────────────────────────

    suspend fun getLikedTracks()                 = db.trackDao().getLiked()
    suspend fun getAllLocalTracks()               = db.trackDao().getAll()
    suspend fun searchLocal(q: String)           = db.trackDao().search(q)
    suspend fun toggleLike(track: Track)         = db.trackDao().setLiked(track.id, !track.isLiked)
    suspend fun saveTrack(track: Track)          = db.trackDao().insert(track)
    suspend fun getAllPlaylists()                 = db.playlistDao().getAll()
    suspend fun getPlaylistTracks(id: String)    = db.playlistDao().getTracksForPlaylist(id)
    suspend fun savePlaylist(pl: Playlist)       = db.playlistDao().insert(pl)
    suspend fun addTrackToPlaylist(plId: String, trackId: String, pos: Int) =
        db.playlistDao().insertPlaylistTrack(PlaylistTrack(plId, trackId, pos))

    suspend fun createPlaylist(name: String): Playlist {
        val pl = Playlist(id = UUID.randomUUID().toString(), name = name)
        db.playlistDao().insert(pl)
        return pl
    }

    suspend fun getServiceAccount(service: MusicService) =
        db.serviceAccountDao().getByService(service.name)

    suspend fun saveServiceAccount(account: ServiceAccount) =
        db.serviceAccountDao().insert(account)

    suspend fun disconnectService(service: MusicService) =
        db.serviceAccountDao().setConnected(service.name, false)

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun SoundCloudTrack.toTrack() = Track(
        id         = "sc_$id",
        title      = title,
        artist     = user.username,
        artworkUrl = artwork_url?.replace("large", "t500x500") ?: "",
        streamUrl  = stream_url ?: "",
        duration   = duration,
        service    = MusicService.SOUNDCLOUD.name,
        serviceId  = id.toString(),
        waveformUrl = waveform_url ?: "",
        genre      = genre ?: "",
        playCount  = playback_count
    )

    private fun SoundCloudPlaylist.toPlaylist() = Playlist(
        id          = "sc_pl_$id",
        name        = title,
        artworkUrl  = artwork_url?.replace("large", "t500x500") ?: "",
        service     = MusicService.SOUNDCLOUD.name,
        serviceId   = id.toString(),
        ownerName   = user.username,
        trackCount  = track_count,
        isImported  = true
    )

    private fun VKTrack.toTrack() = Track(
        id        = "vk_${owner_id}_$id",
        title     = title,
        artist    = artist,
        artworkUrl = album?.thumb?.photo_300 ?: "",
        streamUrl = url,
        duration  = duration * 1000L,
        service   = MusicService.VK.name,
        serviceId = id.toString()
    )

    private fun VKPlaylist.toPlaylist() = Playlist(
        id         = "vk_pl_${owner_id}_$id",
        name       = title,
        artworkUrl = photo?.photo_300 ?: "",
        service    = MusicService.VK.name,
        serviceId  = id.toString(),
        trackCount = count,
        isImported = true
    )

    private fun YandexTrack.toTrack() = Track(
        id        = "yn_$id",
        title     = title,
        artist    = artists.joinToString(", ") { it.name },
        album     = albums.firstOrNull()?.title ?: "",
        artworkUrl = albums.firstOrNull()?.coverUri
            ?.replace("%%", "400x400") ?: "",
        duration  = durationMs,
        service   = MusicService.YANDEX.name,
        serviceId = id.toString()
    )

    private fun YandexPlaylist.toPlaylist() = Playlist(
        id         = "yn_pl_${uid}_$kind",
        name       = title,
        artworkUrl = cover?.uri?.replace("%%", "400x400") ?: "",
        service    = MusicService.YANDEX.name,
        serviceId  = "$uid/$kind",
        trackCount = trackCount,
        isImported = true
    )
}
