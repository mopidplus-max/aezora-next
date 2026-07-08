package com.aezora.next.ui.screens.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.aezora.next.data.models.PlaybackSpeed
import com.aezora.next.ui.theme.*

@Composable
fun PlayerScreen(
    vm: PlayerViewModel,
    onDismiss: () -> Unit,
    onOpenQueue: () -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current
    val track = state.currentTrack ?: return

    // Vinyl rotation animation
    val rotationAnim = rememberInfiniteTransition(label = "vinyl")
    val rotation by rotationAnim.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.playerBg, colors.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.KeyboardArrowDown, "Закрыть",
                        tint = colors.onBackground, modifier = Modifier.size(32.dp))
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Сейчас играет", style = MaterialTheme.typography.labelSmall,
                        color = colors.secondary)
                    Text(track.artist, style = MaterialTheme.typography.titleSmall,
                        color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onOpenQueue) {
                    Icon(Icons.Rounded.QueueMusic, "Очередь",
                        tint = colors.onBackground)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Vinyl disc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Outer vinyl ring
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(if (state.isPlaying) rotation else rotation)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                ) {
                    // Concentric rings
                    listOf(280, 240, 200, 160, 120).forEachIndexed { i, size ->
                        Box(
                            modifier = Modifier
                                .size(size.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .border(
                                    width = if (i == 0) 0.dp else 1.dp,
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = CircleShape
                                )
                        )
                    }
                    // Album art in center
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = "Обложка",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                    )
                }
                // Center hole
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.playerBg)
                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Track info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { vm.player.let { /* toggle like */ } }) {
                    Icon(
                        if (track.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        "Лайк",
                        tint = if (track.isLiked) colors.primary else colors.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = state.progress,
                    onValueChange = { vm.seekTo(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceVariant
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatMs(state.currentPositionMs), style = MaterialTheme.typography.labelSmall,
                        color = colors.secondary)
                    Text(formatMs(state.durationMs), style = MaterialTheme.typography.labelSmall,
                        color = colors.secondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle, "Shuffle",
                        tint = if (state.shuffle) colors.primary else colors.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(
                    onClick = { vm.skipPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.SkipPrevious, "Предыдущий",
                        tint = colors.onBackground, modifier = Modifier.size(36.dp))
                }
                // Play/Pause big button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable { vm.togglePlayPause() }
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        "Воспроизведение",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(
                    onClick = { vm.skipNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, "Следующий",
                        tint = colors.onBackground, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { vm.cycleRepeat() }) {
                    Icon(
                        when (state.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                            Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                            else                   -> Icons.Rounded.Repeat
                        },
                        "Повтор",
                        tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) colors.primary else colors.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Speed / Pitch controls
            SpeedSelector(
                current = state.playbackSpeed,
                onSelect = { vm.setPlaybackSpeed(it) },
                colors = colors
            )

            Spacer(Modifier.height(16.dp))

            // Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.VolumeDown, "Громкость мин",
                    tint = colors.secondary, modifier = Modifier.size(20.dp))
                Slider(
                    value = state.volume,
                    onValueChange = { vm.setVolume(it) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceVariant
                    )
                )
                Icon(Icons.Rounded.VolumeUp, "Громкость макс",
                    tint = colors.secondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SpeedSelector(
    current: PlaybackSpeed,
    onSelect: (PlaybackSpeed) -> Unit,
    colors: AezoraColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PlaybackSpeed.entries.forEach { speed ->
            val selected = speed == current
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.primary else Color.Transparent)
                    .clickable { onSelect(speed) }
                    .padding(vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        speed.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) colors.onPrimary else colors.secondary
                    )
                    Text(
                        when (speed) {
                            PlaybackSpeed.SLOWED  -> "−2 тона"
                            PlaybackSpeed.DEFAULT -> "Оригинал"
                            PlaybackSpeed.SPEEDUP -> "+2 тона"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) colors.onPrimary.copy(0.8f) else colors.secondary.copy(0.6f)
                    )
                }
            }
        }
    }
}

fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
