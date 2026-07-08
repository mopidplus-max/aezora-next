package com.aezora.next.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.aezora.next.data.models.PlaybackSpeed
import com.aezora.next.data.models.QueueItem
import com.aezora.next.data.models.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<QueueItem> = emptyList(),
    val queueIndex: Int = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playbackSpeed: PlaybackSpeed = PlaybackSpeed.DEFAULT,
    val volume: Float = 1f
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    val player: ExoPlayer = ExoPlayer.Builder(app).build()

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
                if (playbackState == Player.STATE_ENDED) {
                    _state.update { it.copy(isPlaying = false, progress = 0f) }
                }
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

    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        val queueItems = queue.map { QueueItem(it) }
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        _state.update { it.copy(queue = queueItems, queueIndex = startIndex, currentTrack = track) }

        val mediaItems = queue.map { t ->
            MediaItem.Builder()
                .setUri(t.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setArtworkUri(android.net.Uri.parse(t.artworkUrl))
                        .build()
                )
                .build()
        }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(fraction: Float) {
        val dur = player.duration.takeIf { it > 0 } ?: return
        player.seekTo((dur * fraction).toLong())
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun skipPrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_state.value.shuffle
        player.shuffleModeEnabled = newShuffle
        _state.update { it.copy(shuffle = newShuffle) }
    }

    fun cycleRepeat() {
        val next = when (_state.value.repeatMode) {
            Player.REPEAT_MODE_OFF  -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL  -> Player.REPEAT_MODE_ONE
            else                    -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    fun setPlaybackSpeed(speed: PlaybackSpeed) {
        val semitones = speed.pitchSemitones
        // pitch shift: each semitone ≈ 2^(1/12) ratio
        val pitchFactor = Math.pow(2.0, semitones / 12.0).toFloat()
        player.playbackParameters = androidx.media3.common.PlaybackParameters(speed.value, pitchFactor)
        _state.update { it.copy(playbackSpeed = speed) }
    }

    fun addToQueue(track: Track) {
        val newItem = QueueItem(track)
        val newQueue = _state.value.queue + newItem
        _state.update { it.copy(queue = newQueue) }
        player.addMediaItem(
            MediaItem.Builder().setUri(track.streamUrl)
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

    override fun onCleared() {
        progressJob?.cancel()
        player.release()
        super.onCleared()
    }
}
