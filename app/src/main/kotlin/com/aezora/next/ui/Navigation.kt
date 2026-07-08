package com.aezora.next.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.aezora.next.ui.components.MiniPlayer
import com.aezora.next.ui.screens.auth.ServicesScreen
import com.aezora.next.ui.screens.home.HomeScreen
import com.aezora.next.ui.screens.library.LibraryScreen
import com.aezora.next.ui.screens.player.PlayerScreen
import com.aezora.next.ui.screens.player.PlayerViewModel
import com.aezora.next.ui.screens.settings.SettingsScreen
import com.aezora.next.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home     : Screen("home",     "Главная",    Icons.Rounded.Home)
    object Library  : Screen("library",  "Библиотека", Icons.Rounded.LibraryMusic)
    object Services : Screen("services", "Сервисы",    Icons.Rounded.Apps)
    object Settings : Screen("settings", "Настройки",  Icons.Rounded.Settings)
}

val bottomScreens = listOf(Screen.Home, Screen.Library, Screen.Services, Screen.Settings)

@Composable
fun AezoraNavHost(
    playerVm: PlayerViewModel,
    currentTheme: AezoraTheme,
    onThemeChange: (AezoraTheme) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showPlayer by remember { mutableStateOf(false) }
    val playerState by playerVm.state.collectAsState()
    val colors = LocalAezoraColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.background,
            bottomBar = {
                Column {
                    // Mini player above nav bar
                    AnimatedVisibility(
                        visible = playerState.currentTrack != null && !showPlayer,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        MiniPlayer(vm = playerVm, onExpand = { showPlayer = true })
                    }

                    NavigationBar(containerColor = colors.surface) {
                        bottomScreens.forEach { screen ->
                            NavigationBarItem(
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(screen.icon, screen.label,
                                        tint = if (currentRoute == screen.route)
                                            colors.primary else colors.secondary)
                                },
                                label = {
                                    Text(screen.label,
                                        color = if (currentRoute == screen.route)
                                            colors.primary else colors.secondary,
                                        style = MaterialTheme.typography.labelSmall)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = colors.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(playerVm = playerVm)
                }
                composable(Screen.Library.route) {
                    LibraryScreen(playerVm = playerVm)
                }
                composable(Screen.Services.route) {
                    ServicesScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onThemeChange = onThemeChange)
                }
            }
        }

        // Full-screen player overlay
        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(
                vm = playerVm,
                onDismiss = { showPlayer = false },
                onOpenQueue = { /* TODO: queue sheet */ }
            )
        }
    }
}
