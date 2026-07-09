package com.aezora.next.ui.screens.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.MusicService
import com.aezora.next.data.models.PlaybackSpeed
import com.aezora.next.data.models.QueueItem
import com.aezora.next.data.models.Track
import com.aezora.next.data.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<QueueItem> = emptyList(),
    val queueIndex: Int = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playbackSpeed: PlaybackSpeed = PlaybackSpeed.DEFAULT,
    val volume: Float = 1f,
    val error: String? = null
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    val player: ExoPlayer = ExoPlayer.Builder(app).build()
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var progressJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressTracking() else progressJob?.cancel()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = player.currentMediaItemIndex
                val queue = _state.value.queue
                if (idx in queue.indices) {
                    _state.update { it.copy(currentTrack = queue[idx].track, queueIndex = idx) }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _state.update { it.copy(isLoading = true) }
                    Player.STATE_READY     -> _state.update { it.copy(isLoading = false, error = null) }
                    Player.STATE_ENDED     -> _state.update { it.copy(isPlaying = false, progress = 0f, isLoading = false) }
                    Player.STATE_IDLE      -> _state.update { it.copy(isLoading = false) }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("Player", "Playback error: ${error.message}")
                _state.update { it.copy(isLoading = false, error = "Ошибка воспроизведения: ${error.message}") }
            }
        })
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val pos = player.currentPosition
                val dur = player.duration.takeIf { it > 0 } ?: 1L
                _state.update {
                    it.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        progress = pos.toFloat() / dur.toFloat()
                    )
                }
                delay(500)
            }
        }
    }

    /**
     * Главный метод запуска трека.
     * Для SoundCloud — резолвит реальный stream URL перед play.
     * Для VK — URL уже в треке, играем сразу.
     */
    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, currentTrack = track) }

            val resolvedTrack = resolveStreamUrl(track)

            if (resolvedTrack.streamUrl.isEmpty()) {
                _state.update { it.copy(isLoading = false, error = "Не удалось получить URL трека") }
                return@launch
            }

            val queueItems = queue.map { QueueItem(it) }
            val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            _state.update { it.copy(queue = queueItems, queueIndex = startIndex) }

            // Для остальных треков в очереди тоже нужен резолвинг,
            // но делаем это лениво — только текущий
            val mediaItems = queue.mapIndexed { idx, t ->
                val url = if (idx == startIndex) resolvedTrack.streamUrl else
                    if (t.streamUrl.isNotEmpty()) t.streamUrl else "placeholder://${t.id}"
                MediaItem.Builder()
                    .setUri(url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setArtworkUri(
                                if (t.artworkUrl.isNotEmpty())
                                    android.net.Uri.parse(t.artworkUrl)
                                else null
                            )
                            .build()
                    )
                    .build()
            }

            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            player.play()
            _state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * При переходе к следующему/предыдущему треку — резолвим URL для него.
     */
    fun skipNext() {
        val nextIndex = _state.value.queueIndex + 1
        val queue = _state.value.queue
        if (nextIndex < queue.size) {
            playTrackAtIndex(nextIndex)
        }
    }

    fun skipPrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else {
            val prevIndex = (_state.value.queueIndex - 1).coerceAtLeast(0)
            playTrackAtIndex(prevIndex)
        }
    }

    private fun playTrackAtIndex(index: Int) {
        val queue = _state.value.queue
        if (index !in queue.indices) return
        val track = queue[index].track
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentTrack = track, queueIndex = index) }
            val resolved = resolveStreamUrl(track)
            if (resolved.streamUrl.isEmpty()) {
                _state.update { it.copy(isLoading = false, error = "Нет URL") }
                return@launch
            }
            // Обновляем MediaItem для этого индекса
            val newItem = MediaItem.Builder()
                .setUri(resolved.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title).setArtist(track.artist).build()
                ).build()
            player.replaceMediaItem(index, newItem)
            player.seekTo(index, 0L)
            player.play()
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun resolveStreamUrl(track: Track): Track {
        if (track.streamUrl.isNotEmpty()) return track
        return when (track.service) {
            MusicService.SOUNDCLOUD.name -> {
                val url = repo.resolveSoundCloudStreamUrl(track.serviceId)
                track.copy(streamUrl = url)
            }
            else -> track
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(fraction: Float) {
        val dur = player.duration.takeIf { it > 0 } ?: return
        player.seekTo((dur * fraction).toLong())
    }

    fun toggleShuffle() {
        val new = !_state.value.shuffle
        player.shuffleModeEnabled = new
        _state.update { it.copy(shuffle = new) }
    }

    fun cycleRepeat() {
        val next = when (_state.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else                   -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    fun setPlaybackSpeed(speed: PlaybackSpeed) {
        val pitchFactor = Math.pow(2.0, speed.pitchSemitones / 12.0).toFloat()
        player.playbackParameters = androidx.media3.common.PlaybackParameters(speed.value, pitchFactor)
        _state.update { it.copy(playbackSpeed = speed) }
    }

    fun addToQueue(track: Track) {
        val newQueue = _state.value.queue + QueueItem(track)
        _state.update { it.copy(queue = newQueue) }
        // Placeholder URI — реально резолвим при воспроизведении
        val placeholder = if (track.streamUrl.isNotEmpty()) track.streamUrl else "placeholder://${track.id}"
        player.addMediaItem(
            MediaItem.Builder().setUri(placeholder)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title).setArtist(track.artist).build()
                ).build()
        )
    }

    fun removeFromQueue(index: Int) {
        val newQueue = _state.value.queue.toMutableList().also { it.removeAt(index) }
        _state.update { it.copy(queue = newQueue) }
        player.removeMediaItem(index)
    }

    fun setVolume(vol: Float) {
        player.volume = vol
        _state.update { it.copy(volume = vol) }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        progressJob?.cancel()
        player.release()
        super.onCleared()
    }
}
