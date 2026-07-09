package com.aezora.next.data.repository

import android.util.Log
import com.aezora.next.BuildConfig
import com.aezora.next.data.api.*
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID

class MusicRepository(private val db: AezoraDatabase) {

    private val scApiV2 = NetworkClient.soundCloudApiV2
    private val vkApi   = NetworkClient.vkApi
    private val ynApi   = NetworkClient.yandexApi
    private val clientId = BuildConfig.SOUNDCLOUD_CLIENT_ID

    private val httpClient = OkHttpClient()

    // ── SoundCloud ────────────────────────────────────────────────────────────

    suspend fun searchSoundCloud(query: String, offset: Int = 0): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.searchTracks(query, clientId, offset = offset)
                result.collection.map { it.toTrack() }
            } catch (e: Exception) {
                Log.e("Repo", "SC search error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getSoundCloudTrending(): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getTrending(clientId = clientId)
                result.collection.map { it.track.toTrack() }
            } catch (e: Exception) {
                Log.e("Repo", "SC trending error: ${e.message}")
                emptyList()
            }
        }

    /**
     * Резолвит реальный MP3 URL для трека SoundCloud.
     * Сначала ищет progressive-транскодинг (прямой MP3).
     * Если не нашёл — берёт первый доступный и запрашивает URL у него.
     */
    suspend fun resolveSoundCloudStreamUrl(serviceId: String): String =
        withContext(Dispatchers.IO) {
            try {
                val track = scApiV2.getTrack(serviceId.toLong(), clientId)
                val transcodings = track.media?.transcodings ?: return@withContext ""

                // Предпочитаем progressive (прямой mp3), иначе hls
                val transcoding = transcodings.firstOrNull {
                    it.format.protocol == "progressive"
                } ?: transcodings.firstOrNull {
                    it.format.protocol == "hls"
                } ?: return@withContext ""

                // Запрашиваем реальный URL у эндпоинта транскодинга
                val streamEndpoint = "${transcoding.url}?client_id=$clientId"
                val req = Request.Builder().url(streamEndpoint).build()
                val resp = httpClient.newCall(req).execute()
                val body = resp.body?.string() ?: return@withContext ""
                val json = JSONObject(body)
                json.optString("url", "")
            } catch (e: Exception) {
                Log.e("Repo", "SC stream resolve error: ${e.message}")
                ""
            }
        }

    suspend fun getSoundCloudLikes(token: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getLikedTracks("OAuth $token", clientId)
                result.collection.map { it.toTrack() }
            } catch (e: Exception) {
                Log.e("Repo", "SC likes error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getSoundCloudPlaylists(token: String): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = scApiV2.getMyPlaylists("OAuth $token", clientId)
                result.collection.map { it.toPlaylist() }
            } catch (e: Exception) {
                Log.e("Repo", "SC playlists error: ${e.message}")
                emptyList()
            }
        }

    // ── VK ────────────────────────────────────────────────────────────────────

    suspend fun searchVK(token: String, query: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.searchAudio(token, query)
                result.response?.items?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) {
                Log.e("Repo", "VK search error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getVKLibrary(token: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.getAudio(token)
                result.response?.items?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) {
                Log.e("Repo", "VK library error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getVKPlaylists(token: String, ownerId: Long): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = vkApi.getPlaylists(token, ownerId)
                result.response?.items?.map { it.toPlaylist() } ?: emptyList()
            } catch (e: Exception) {
                Log.e("Repo", "VK playlists error: ${e.message}")
                emptyList()
            }
        }

    // ── Yandex ───────────────────────────────────────────────────────────────

    suspend fun searchYandex(token: String, query: String): List<Track> =
        withContext(Dispatchers.IO) {
            try {
                val result = ynApi.search(query, auth = "OAuth $token")
                result.result?.tracks?.results?.map { it.toTrack() } ?: emptyList()
            } catch (e: Exception) {
                Log.e("Repo", "Yandex search error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getYandexPlaylists(token: String, uid: Long): List<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val result = ynApi.getUserPlaylists(uid, "OAuth $token")
                result.result?.map { it.toPlaylist() } ?: emptyList()
            } catch (e: Exception) {
                Log.e("Repo", "Yandex playlists error: ${e.message}")
                emptyList()
            }
        }

    suspend fun getYandexAccountUid(token: String): Long =
        withContext(Dispatchers.IO) {
            try {
                val result = ynApi.getAccountStatus("OAuth $token")
                result.result?.account?.uid ?: 0L
            } catch (e: Exception) { 0L }
        }

    // ── Local DB ──────────────────────────────────────────────────────────────

    suspend fun getLikedTracks() = db.trackDao().getLiked()
    suspend fun getAllLocalTracks() = db.trackDao().getAll()
    suspend fun searchLocal(q: String) = db.trackDao().search(q)

    /** Сохраняет трек в DB, затем переключает лайк */
    suspend fun toggleLike(track: Track) {
        // Убеждаемся что трек есть в базе
        val existing = db.trackDao().getById(track.id)
        if (existing == null) {
            db.trackDao().insert(track)
        }
        val current = db.trackDao().getById(track.id) ?: track
        db.trackDao().setLiked(current.id, !current.isLiked)
    }

    suspend fun isLiked(trackId: String): Boolean =
        db.trackDao().getById(trackId)?.isLiked ?: false

    suspend fun saveTrack(track: Track) = db.trackDao().insert(track)
    suspend fun getAllPlaylists() = db.playlistDao().getAll()
    suspend fun getPlaylistTracks(id: String) = db.playlistDao().getTracksForPlaylist(id)
    suspend fun savePlaylist(pl: Playlist) = db.playlistDao().insert(pl)

    suspend fun addTrackToPlaylist(plId: String, trackId: String, pos: Int) =
        db.playlistDao().insertPlaylistTrack(PlaylistTrack(plId, trackId, pos))

    suspend fun createPlaylist(name: String): Playlist {
        val pl = Playlist(id = UUID.randomUUID().toString(), name = name)
        db.playlistDao().insert(pl)
        return pl
    }

    suspend fun deletePlaylist(pl: Playlist) = db.playlistDao().delete(pl)

    suspend fun getServiceAccount(service: MusicService) =
        db.serviceAccountDao().getByService(service.name)

    suspend fun getAllServiceAccounts() = db.serviceAccountDao().getAll()

    suspend fun saveServiceAccount(account: ServiceAccount) =
        db.serviceAccountDao().insert(account)

    suspend fun disconnectService(service: MusicService) =
        db.serviceAccountDao().setConnected(service.name, false)

    /**
     * При подключении сервиса — сразу синхронизируем плейлисты.
     * Возвращает количество импортированных плейлистов.
     */
    suspend fun syncServicePlaylists(service: MusicService, token: String): Int =
        withContext(Dispatchers.IO) {
            try {
                val playlists: List<Playlist> = when (service) {
                    MusicService.SOUNDCLOUD -> getSoundCloudPlaylists(token)
                    MusicService.VK -> {
                        // ownerId из токена VK не известен заранее — сохраняем пустой список
                        // Пользователь может позже импортировать через ссылку
                        emptyList()
                    }
                    MusicService.YANDEX -> {
                        val uid = getYandexAccountUid(token)
                        if (uid > 0) getYandexPlaylists(token, uid) else emptyList()
                    }
                    MusicService.YOUTUBE -> emptyList()
                    MusicService.LOCAL -> emptyList()
                }
                playlists.forEach { db.playlistDao().insert(it) }
                playlists.size
            } catch (e: Exception) {
                Log.e("Repo", "Sync playlists error: ${e.message}")
                0
            }
        }

    // ── Mappers ───────────────────────────────────────────────────────────────

    fun SoundCloudTrack.toTrack() = Track(
        id          = "sc_$id",
        title       = title,
        artist      = user.username,
        artworkUrl  = artwork_url?.replace("large", "t500x500") ?: "",
        // streamUrl оставляем пустым — резолвим lazy при воспроизведении
        streamUrl   = "",
        duration    = duration,
        service     = MusicService.SOUNDCLOUD.name,
        serviceId   = id.toString(),
        waveformUrl = waveform_url ?: "",
        genre       = genre ?: "",
        playCount   = playback_count
    )

    private fun SoundCloudPlaylist.toPlaylist() = Playlist(
        id         = "sc_pl_$id",
        name       = title,
        artworkUrl = artwork_url?.replace("large", "t500x500") ?: "",
        service    = MusicService.SOUNDCLOUD.name,
        serviceId  = id.toString(),
        ownerName  = user.username,
        trackCount = track_count,
        isImported = true
    )

    private fun VKTrack.toTrack() = Track(
        id         = "vk_${owner_id}_$id",
        title      = title,
        artist     = artist,
        artworkUrl = album?.thumb?.photo_300 ?: "",
        streamUrl  = url,      // VK даёт прямой URL
        duration   = duration * 1000L,
        service    = MusicService.VK.name,
        serviceId  = id.toString()
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
        id         = "yn_$id",
        title      = title,
        artist     = artists.joinToString(", ") { it.name },
        album      = albums.firstOrNull()?.title ?: "",
        artworkUrl = albums.firstOrNull()?.coverUri?.replace("%%", "400x400") ?: "",
        duration   = durationMs,
        service    = MusicService.YANDEX.name,
        serviceId  = id.toString()
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
