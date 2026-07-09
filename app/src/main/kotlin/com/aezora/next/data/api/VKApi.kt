package com.aezora.next.data.api

import com.aezora.next.data.models.*
import retrofit2.http.*

interface VKApi {

    @GET("audio.get")
    suspend fun getAudio(
        @Query("access_token") token: String,
        @Query("count") count: Int = 100,
        @Query("v") version: String = "5.131"
    ): VKResponse<VKAudioList>

    @GET("audio.search")
    suspend fun searchAudio(
        @Query("access_token") token: String,
        @Query("q") query: String,
        @Query("count") count: Int = 30,
        @Query("v") version: String = "5.131"
    ): VKResponse<VKAudioList>

    @GET("audio.getPlaylists")
    suspend fun getPlaylists(
        @Query("access_token") token: String,
        @Query("owner_id") ownerId: Long,
        @Query("count") count: Int = 50,
        @Query("v") version: String = "5.131"
    ): VKResponse<VKPlaylistList>

    @GET("audio.get")
    suspend fun getPlaylistTracks(
        @Query("access_token") token: String,
        @Query("owner_id") ownerId: Long,
        @Query("album_id") albumId: Long,
        @Query("v") version: String = "5.131"
    ): VKResponse<VKAudioList>
}

data class VKResponse<T>(val response: T? = null, val error: VKError? = null)
data class VKError(val error_code: Int = 0, val error_msg: String = "")
data class VKAudioList(val count: Int = 0, val items: List<VKTrack> = emptyList())
data class VKPlaylistList(val count: Int = 0, val items: List<VKPlaylist> = emptyList())
