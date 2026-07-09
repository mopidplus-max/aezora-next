package com.aezora.next.ui.screens.library

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
import com.aezora.next.data.models.*
import com.aezora.next.ui.screens.home.TrackRow
import com.aezora.next.ui.screens.player.PlayerViewModel
import com.aezora.next.ui.theme.*

@Composable
fun LibraryScreen(
    playerVm: PlayerViewModel,
    vm: LibraryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var importUrl by remember { mutableStateOf("") }

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
            Text(
                "Библиотека",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showImportDialog = true }) {
                Icon(Icons.Rounded.Download, "Импорт", tint = colors.secondary)
            }
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Rounded.Add, "Создать", tint = colors.primary)
            }
        }

        // Tabs
        val tabs = listOf("Плейлисты", "Избранное")
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = colors.background,
            contentColor = colors.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = state.selectedTab == idx,
                    onClick = { vm.selectTab(idx) },
                    text = {
                        Text(
                            title,
                            color = if (state.selectedTab == idx) colors.primary else colors.secondary
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Column
        }

        when (state.selectedTab) {
            0 -> PlaylistsTab(state.playlists, colors)
            1 -> LikedTab(state.likedTracks, playerVm, colors)
        }
    }

    // Create playlist dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = colors.surface,
            title = { Text("Новый плейлист", color = colors.onBackground) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Название...", color = colors.secondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.surfaceVariant,
                        focusedTextColor = colors.onBackground,
                        unfocusedTextColor = colors.onBackground
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        vm.createPlaylist(newPlaylistName)
                        newPlaylistName = ""
                        showCreateDialog = false
                    }
                }) { Text("Создать", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Отмена", color = colors.secondary)
                }
            }
        )
    }

    // Import dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = colors.surface,
            title = { Text("Импорт плейлиста", color = colors.onBackground) },
            text = {
                Column {
                    Text(
                        "Вставьте ссылку на плейлист из\nSoundCloud, VK или Яндекс Музыки",
                        color = colors.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        placeholder = { Text("https://...", color = colors.secondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.surfaceVariant,
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importUrl.isNotBlank()) {
                        vm.importPlaylistFromUrl(importUrl)
                        importUrl = ""
                        showImportDialog = false
                    }
                }) { Text("Импорт", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Отмена", color = colors.secondary)
                }
            }
        )
    }
}

@Composable
fun PlaylistsTab(playlists: List<Playlist>, colors: AezoraColorScheme) {
    if (playlists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.LibraryMusic, null,
                    tint = colors.secondary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("Нет плейлистов", color = colors.secondary,
                    style = MaterialTheme.typography.titleMedium)
                Text("Создайте или импортируйте плейлист",
                    color = colors.secondary.copy(0.6f),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        items(playlists) { playlist -> PlaylistRow(playlist, colors) }
    }
}

@Composable
fun PlaylistRow(playlist: Playlist, colors: AezoraColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (playlist.artworkUrl.isNotEmpty()) {
                AsyncImage(
                    model = playlist.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Rounded.MusicNote, null, tint = colors.secondary)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(playlist.name, style = MaterialTheme.typography.titleSmall,
                color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (playlist.isImported) {
                    ServiceBadge(playlist.service, colors)
                    Spacer(Modifier.width(6.dp))
                }
                Text("${playlist.trackCount} треков",
                    style = MaterialTheme.typography.bodySmall, color = colors.secondary)
            }
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = colors.secondary)
    }
}

@Composable
fun ServiceBadge(serviceName: String, colors: AezoraColorScheme) {
    val (label, color) = when (serviceName) {
        MusicService.SOUNDCLOUD.name -> "SC" to SoundCloudOrange
        MusicService.VK.name        -> "VK" to VKBlue
        MusicService.YANDEX.name    -> "YM" to YandexYellow
        else                        -> "LC" to colors.primary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun LikedTab(tracks: List<Track>, playerVm: PlayerViewModel, colors: AezoraColorScheme) {
    if (tracks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.FavoriteBorder, null,
                    tint = colors.secondary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("Нет избранных треков", color = colors.secondary)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        items(tracks) { track ->
            TrackRow(track, colors) { playerVm.playTrack(track, tracks) }
        }
    }
}
