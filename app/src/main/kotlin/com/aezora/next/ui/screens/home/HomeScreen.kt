package com.aezora.next.ui.screens.home

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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.aezora.next.data.models.Track
import com.aezora.next.ui.screens.player.PlayerViewModel
import com.aezora.next.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playerVm: PlayerViewModel,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Привет 👋", style = MaterialTheme.typography.labelMedium, color = colors.secondary)
                Text("Aezora Next", style = MaterialTheme.typography.headlineLarge, color = colors.onBackground)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, "Профиль", tint = colors.onSurface)
            }
        }

        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = vm::onQueryChange,
            placeholder = { Text("Поиск треков, артистов...", color = colors.secondary) },
            leadingIcon = { Icon(Icons.Rounded.Search, "Поиск", tint = colors.secondary) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { vm.onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, "Очистить", tint = colors.secondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.surfaceVariant,
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                cursorColor = colors.primary,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            ),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Search results
            if (state.searchQuery.length > 2) {
                item {
                    SectionHeader("Результаты поиска", colors)
                }
                if (state.isSearching) {
                    item {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                } else {
                    items(state.searchResults) { track ->
                        TrackRow(track = track, colors = colors, onClick = {
                            playerVm.playTrack(track, state.searchResults)
                        })
                    }
                }
                return@LazyColumn
            }

            // Trending section
            item { SectionHeader("В тренде на SoundCloud", colors) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.trending.take(10)) { track ->
                        TrackCard(track = track, colors = colors, onClick = {
                            playerVm.playTrack(track, state.trending)
                        })
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader("Все треки", colors) }

            items(state.trending) { track ->
                TrackRow(track = track, colors = colors, onClick = {
                    playerVm.playTrack(track, state.trending)
                })
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, colors: AezoraColorScheme) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        color = colors.onBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun TrackCard(track: Track, colors: AezoraColorScheme, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = track.artworkUrl.ifEmpty { null },
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
        )
        Spacer(Modifier.height(6.dp))
        Text(track.title, style = MaterialTheme.typography.titleSmall,
            color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(track.artist, style = MaterialTheme.typography.bodySmall,
            color = colors.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun TrackRow(track: Track, colors: AezoraColorScheme, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl.ifEmpty { null },
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.titleSmall,
                color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.bodySmall,
                color = colors.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Rounded.MoreVert, "Меню", tint = colors.secondary)
    }
}
