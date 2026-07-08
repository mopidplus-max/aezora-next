package com.aezora.next.data.api

import com.aezora.next.data.models.*
import retrofit2.http.*

interface SoundCloudApi {

    @GET("search/tracks")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SoundCloudSearchResult

    @GET("tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: Long,
        @Query("client_id") clientId: String
    ): SoundCloudTrack

    @GET("tracks/{id}/streams")
    suspend fun getStreamUrl(
        @Path("id") id: Long,
        @Query("client_id") clientId: String
    ): Map<String, String>

    @GET("playlists/{id}")
    suspend fun getPlaylist(
        @Path("id") id: Long,
        @Query("client_id") clientId: String
    ): SoundCloudPlaylist

    @GET("me/tracks")
    suspend fun getLikedTracks(
        @Header("Authorization") auth: String,
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 50
    ): List<SoundCloudTrack>

    @GET("me/playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") auth: String,
        @Query("client_id") clientId: String
    ): List<SoundCloudPlaylist>

    @GET("charts")
    suspend fun getCharts(
        @Query("kind") kind: String = "trending",
        @Query("genre") genre: String = "soundcloud:genres:all-music",
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 20
    ): SoundCloudChartsResponse
}

data class SoundCloudChartsResponse(
    val collection: List<SoundCloudChartItem> = emptyList()
)
data class SoundCloudChartItem(val track: SoundCloudTrack = SoundCloudTrack())
