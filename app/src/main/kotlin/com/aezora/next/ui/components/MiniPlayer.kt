package com.aezora.next.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.aezora.next.ui.screens.player.PlayerViewModel
import com.aezora.next.ui.theme.*

@Composable
fun MiniPlayer(
    vm: PlayerViewModel,
    onExpand: () -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current
    val track = state.currentTrack ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable { onExpand() }
    ) {
        // Progress indicator at top
        Box(
            modifier = Modifier
                .fillMaxWidth(state.progress)
                .height(2.dp)
                .background(colors.primary)
                .align(Alignment.TopStart)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            AsyncImage(
                model = track.artworkUrl.ifEmpty { null },
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controls
            IconButton(onClick = { vm.skipPrevious() }) {
                Icon(Icons.Rounded.SkipPrevious, "Предыдущий",
                    tint = colors.onBackground, modifier = Modifier.size(24.dp))
            }
            IconButton(
                onClick = { vm.togglePlayPause() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
            ) {
                Icon(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    "Воспроизведение",
                    tint = colors.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = { vm.skipNext() }) {
                Icon(Icons.Rounded.SkipNext, "Следующий",
                    tint = colors.onBackground, modifier = Modifier.size(24.dp))
            }
        }
    }
}
