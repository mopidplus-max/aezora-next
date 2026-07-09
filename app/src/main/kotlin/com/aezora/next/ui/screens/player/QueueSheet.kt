package com.aezora.next.ui.screens.player

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import com.aezora.next.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(vm: PlayerViewModel, onDismiss: () -> Unit) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Очередь",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${state.queue.size} треков",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondary
                )
            }
            HorizontalDivider(color = colors.surfaceVariant)
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(state.queue) { idx, item ->
                    val isCurrent = idx == state.queueIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) colors.primary.copy(0.08f) else colors.surface)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCurrent) {
                            Icon(Icons.Rounded.VolumeUp, null,
                                tint = colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Text("${idx + 1}", style = MaterialTheme.typography.labelSmall,
                                color = colors.secondary,
                                modifier = Modifier.width(28.dp))
                        }
                        AsyncImage(
                            model = item.track.artworkUrl.ifEmpty { null },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.track.title, style = MaterialTheme.typography.titleSmall,
                                color = if (isCurrent) colors.primary else colors.onBackground,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.track.artist, style = MaterialTheme.typography.bodySmall,
                                color = colors.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (!isCurrent) {
                            IconButton(onClick = { vm.removeFromQueue(idx) }) {
                                Icon(Icons.Rounded.Close, "Удалить", tint = colors.secondary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
