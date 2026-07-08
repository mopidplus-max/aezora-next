package com.aezora.next.data.api

import com.aezora.next.data.models.*
import retrofit2.http.*

interface YandexMusicApi {

    @GET("users/{uid}/playlists/list")
    suspend fun getUserPlaylists(
        @Path("uid") uid: Long,
        @Header("Authorization") auth: String
    ): YandexResponse<List<YandexPlaylist>>

    @GET("users/{uid}/playlists/{kind}")
    suspend fun getPlaylist(
        @Path("uid") uid: Long,
        @Path("kind") kind: Long,
        @Header("Authorization") auth: String
    ): YandexResponse<YandexPlaylistFull>

    @GET("search")
    suspend fun search(
        @Query("text") text: String,
        @Query("type") type: String = "track",
        @Query("page") page: Int = 0,
        @Header("Authorization") auth: String
    ): YandexResponse<YandexSearchResult>

    @GET("tracks/{id}/download-info")
    suspend fun getDownloadInfo(
        @Path("id") id: Long,
        @Header("Authorization") auth: String
    ): YandexResponse<List<YandexDownloadInfo>>

    @GET("account/status")
    suspend fun getAccountStatus(
        @Header("Authorization") auth: String
    ): YandexResponse<YandexAccountStatus>
}

data class YandexResponse<T>(val result: T? = null, val status: String = "")
data class YandexPlaylistFull(
    val uid: Long = 0,
    val kind: Long = 0,
    val title: String = "",
    val trackCount: Int = 0,
    val tracks: List<YandexTrackShort> = emptyList()
)
data class YandexTrackShort(val id: Long = 0, val timestamp: String = "")
data class YandexSearchResult(val tracks: YandexTrackResults? = null)
data class YandexTrackResults(val results: List<YandexTrack> = emptyList(), val total: Int = 0)
data class YandexDownloadInfo(
    val codec: String = "",
    val gain: Boolean = false,
    val preview: Boolean = false,
    val downloadInfoUrl: String = "",
    val bitrateInKbps: Int = 0
)
data class YandexAccountStatus(val account: YandexAccount = YandexAccount())
data class YandexAccount(val uid: Long = 0, val login: String = "", val displayName: String = "")
