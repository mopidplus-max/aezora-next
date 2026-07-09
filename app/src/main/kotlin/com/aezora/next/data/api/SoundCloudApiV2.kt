package com.aezora.next.data.api

import com.aezora.next.data.models.*
import retrofit2.http.*

interface SoundCloudApiV2 {

    @GET("search/tracks")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("app_version") appVersion: String = "1.0.0"
    ): SoundCloudSearchResult

    @GET("tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: Long,
        @Query("client_id") clientId: String
    ): SoundCloudTrack

    @GET("stream/tracks/{id}")
    suspend fun resolveStreamUrl(
        @Path("id") id: Long,
        @Query("client_id") clientId: String
    ): StreamUrlResponse

    @GET("charts")
    suspend fun getTrending(
        @Query("kind") kind: String = "trending",
        @Query("genre") genre: String = "soundcloud:genres:all-music",
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 20
    ): SoundCloudChartsResponse

    @GET("me/library/likes/tracks")
    suspend fun getLikedTracks(
        @Header("Authorization") auth: String,
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 50
    ): SoundCloudSearchResult

    @GET("me/playlists/liked_and_owned")
    suspend fun getMyPlaylists(
        @Header("Authorization") auth: String,
        @Query("client_id") clientId: String
    ): SoundCloudPlaylistsResponse
}

data class SoundCloudPlaylistsResponse(
    val collection: List<SoundCloudPlaylist> = emptyList()
)
