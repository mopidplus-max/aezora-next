package com.aezora.next.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.Track
import com.aezora.next.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val trending: List<Track> = emptyList(),
    val searchResults: List<Track> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val likedIds: Set<String> = emptySet(),
    val error: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadTrending()
        loadLikedIds()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val tracks = repo.getSoundCloudTrending()
            _state.update {
                it.copy(
                    trending = tracks,
                    isLoading = false,
                    error = if (tracks.isEmpty()) "Не удалось загрузить треки" else null
                )
            }
        }
    }

    private fun loadLikedIds() {
        viewModelScope.launch {
            val liked = repo.getLikedTracks()
            _state.update { it.copy(likedIds = liked.map { t -> t.id }.toSet()) }
        }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(searchQuery = q) }
        if (q.length > 2) search(q)
        else _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
    }

    private fun search(q: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val results = repo.searchSoundCloud(q)
            _state.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun toggleLike(track: Track) {
        viewModelScope.launch {
            repo.toggleLike(track)
            loadLikedIds()
        }
    }

    fun isLiked(trackId: String) = _state.value.likedIds.contains(trackId)
}
