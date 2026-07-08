package com.aezora.next.ui.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.*
import com.aezora.next.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(
    val likedTracks: List<Track> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0 // 0=Плейлисты, 1=Избранное, 2=Импорт
)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))
    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val liked = repo.getLikedTracks()
            val playlists = repo.getAllPlaylists()
            _state.update { it.copy(likedTracks = liked, playlists = playlists, isLoading = false) }
        }
    }

    fun selectTab(tab: Int) = _state.update { it.copy(selectedTab = tab) }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repo.createPlaylist(name)
            load()
        }
    }

    fun importPlaylistFromUrl(url: String) {
        viewModelScope.launch {
            // Detect service from URL
            val service = when {
                "soundcloud.com" in url -> MusicService.SOUNDCLOUD
                "vk.com" in url        -> MusicService.VK
                "music.yandex" in url  -> MusicService.YANDEX
                else -> null
            }
            // Import logic handled per-service in repository
            // For now create a placeholder playlist
            val pl = Playlist(
                id = java.util.UUID.randomUUID().toString(),
                name = "Импорт из ${service?.displayName ?: "неизвестного сервиса"}",
                service = service?.name ?: MusicService.LOCAL.name,
                isImported = true
            )
            repo.savePlaylist(pl)
            load()
        }
    }
}
