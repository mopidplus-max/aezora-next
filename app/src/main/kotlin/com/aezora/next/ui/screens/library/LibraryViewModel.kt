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
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val message: String? = null
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
            _state.update {
                it.copy(likedTracks = liked, playlists = playlists, isLoading = false)
            }
        }
    }

    fun selectTab(tab: Int) = _state.update { it.copy(selectedTab = tab) }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repo.createPlaylist(name)
            load()
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repo.deletePlaylist(playlist)
            load()
        }
    }

    fun unlikeTrack(track: Track) {
        viewModelScope.launch {
            repo.toggleLike(track)
            load()
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
