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
    val isSearching: Boolean = false
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init { loadTrending() }

    fun loadTrending() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val tracks = repo.getSoundCloudTrending()
            _state.update { it.copy(trending = tracks, isLoading = false) }
        }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(searchQuery = q) }
        if (q.length > 2) search(q) else _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
    }

    private fun search(q: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val results = repo.searchSoundCloud(q)
            _state.update { it.copy(searchResults = results, isSearching = false) }
        }
    }
}
