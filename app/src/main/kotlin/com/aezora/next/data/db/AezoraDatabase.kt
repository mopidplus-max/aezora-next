package com.aezora.next.data.db

import android.content.Context
import androidx.room.*
import com.aezora.next.data.models.*

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class, ServiceAccount::class],
    version = 1,
    exportSchema = false
)
abstract class AezoraDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun serviceAccountDao(): ServiceAccountDao

    companion object {
        @Volatile private var INSTANCE: AezoraDatabase? = null
        fun getInstance(context: Context): AezoraDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AezoraDatabase::class.java, "aezora_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY addedAt DESC")
    suspend fun getAll(): List<Track>

    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY addedAt DESC")
    suspend fun getLiked(): List<Track>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: String): Track?

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<Track>)

    @Update
    suspend fun update(track: Track)

    @Delete
    suspend fun delete(track: Track)

    @Query("UPDATE tracks SET isLiked = :liked WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    suspend fun getAll(): List<Playlist>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: String): Playlist?

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getWithTracks(id: String): PlaylistWithTracks?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(pt: PlaylistTrack)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    @Query("SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON t.id = pt.trackId WHERE pt.playlistId = :playlistId ORDER BY pt.position")
    suspend fun getTracksForPlaylist(playlistId: String): List<Track>

    @Query("UPDATE playlists SET trackCount = :count WHERE id = :id")
    suspend fun updateTrackCount(id: String, count: Int)
}

@Dao
interface ServiceAccountDao {
    @Query("SELECT * FROM service_accounts")
    suspend fun getAll(): List<ServiceAccount>

    @Query("SELECT * FROM service_accounts WHERE service = :service")
    suspend fun getByService(service: String): ServiceAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: ServiceAccount)

    @Delete
    suspend fun delete(account: ServiceAccount)

    @Query("UPDATE service_accounts SET isConnected = :connected WHERE service = :service")
    suspend fun setConnected(service: String, connected: Boolean)
}
