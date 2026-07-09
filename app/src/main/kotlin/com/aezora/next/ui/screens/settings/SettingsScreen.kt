package com.aezora.next.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.aezora.next.ui.theme.*

val Context.dataStore by preferencesDataStore(name = "aezora_prefs")
val THEME_KEY = stringPreferencesKey("theme")
val QUALITY_KEY = stringPreferencesKey("quality")

data class SettingsState(
    val theme: AezoraTheme = AezoraTheme.BLUE_VIOLET,
    val streamingQuality: String = "HIGH",
    val downloadQuality: String = "HIGH",
    val crossfadeSec: Int = 0,
    val normalizeVolume: Boolean = true,
    val showLyrics: Boolean = true,
    val cacheSize: String = "512 MB"
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.dataStore.data.collect { prefs ->
                val themeName = prefs[THEME_KEY] ?: AezoraTheme.BLUE_VIOLET.name
                _state.update { it.copy(theme = AezoraTheme.valueOf(themeName)) }
            }
        }
    }

    fun setTheme(theme: AezoraTheme) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { it[THEME_KEY] = theme.name }
            _state.update { it.copy(theme = theme) }
        }
    }

    fun setStreamingQuality(q: String) = _state.update { it.copy(streamingQuality = q) }
    fun setCrossfade(sec: Int)         = _state.update { it.copy(crossfadeSec = sec) }
    fun setNormalize(v: Boolean)       = _state.update { it.copy(normalizeVolume = v) }
    fun setShowLyrics(v: Boolean)      = _state.update { it.copy(showLyrics = v) }
}

@Composable
fun SettingsScreen(
    vm: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onThemeChange: (AezoraTheme) -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Настройки",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.onBackground,
            modifier = Modifier.padding(20.dp)
        )

        // ── Оформление ────────────────────────────────────────────────────
        SettingsSection("Оформление", colors) {
            Text(
                "Тема",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeChip("Тёмная", AezoraTheme.DARK_WHITE,
                    DarkWhiteColors.Primary, state.theme, colors) {
                    vm.setTheme(it); onThemeChange(it)
                }
                ThemeChip("Зелёная", AezoraTheme.YELLOW_GREEN,
                    YellowGreenColors.Primary, state.theme, colors) {
                    vm.setTheme(it); onThemeChange(it)
                }
                ThemeChip("Фиолет", AezoraTheme.BLUE_VIOLET,
                    BlueVioletColors.Primary, state.theme, colors) {
                    vm.setTheme(it); onThemeChange(it)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))

        // ── Воспроизведение ───────────────────────────────────────────────
        SettingsSection("Воспроизведение", colors) {
            SettingsDropdown(
                label = "Качество стриминга",
                value = state.streamingQuality,
                options = listOf("LOW (64 kbps)", "MEDIUM (128 kbps)", "HIGH (320 kbps)"),
                colors = colors,
                onSelect = { vm.setStreamingQuality(it) }
            )
            SettingsToggle(
                label = "Нормализация громкости",
                desc = "Выравнивает громкость треков",
                checked = state.normalizeVolume,
                colors = colors,
                onToggle = { vm.setNormalize(it) }
            )
            SettingsSliderRow(
                label = "Кроссфейд",
                value = state.crossfadeSec.toFloat(),
                range = 0f..12f,
                displayValue = if (state.crossfadeSec == 0) "Выкл" else "${state.crossfadeSec} сек",
                colors = colors,
                onValue = { vm.setCrossfade(it.toInt()) }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Плеер ─────────────────────────────────────────────────────────
        SettingsSection("Плеер", colors) {
            SettingsToggle(
                label = "Показывать текст песни",
                desc = "Авто-поиск текста для трека",
                checked = state.showLyrics,
                colors = colors,
                onToggle = { vm.setShowLyrics(it) }
            )
            SettingsInfoRow("Кэш", state.cacheSize, Icons.Rounded.Storage, colors)
            SettingsActionRow("Очистить кэш", Icons.Rounded.DeleteSweep, colors) {}
        }

        Spacer(Modifier.height(12.dp))

        // ── О приложении ──────────────────────────────────────────────────
        SettingsSection("О приложении", colors) {
            SettingsInfoRow("Версия", "1.0.0", Icons.Rounded.Info, colors)
            SettingsInfoRow("Разработчик", "Aezora Team", Icons.Rounded.Code, colors)
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun ThemeChip(
    label: String,
    theme: AezoraTheme,
    accentColor: Color,
    current: AezoraTheme,
    colors: AezoraColorScheme,
    onClick: (AezoraTheme) -> Unit
) {
    val selected = current == theme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accentColor else colors.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (selected) accentColor.copy(0.1f) else colors.surface)
            .clickable { onClick(theme) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onBackground else colors.secondary)
    }
}

@Composable
fun SettingsSection(title: String, colors: AezoraColorScheme, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = colors.secondary,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp)
        )
        content()
    }
}

@Composable
fun SettingsToggle(label: String, desc: String, checked: Boolean,
                   colors: AezoraColorScheme, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = colors.onBackground)
            if (desc.isNotEmpty())
                Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.secondary)
        }
        Switch(
            checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary)
        )
    }
}

@Composable
fun SettingsDropdown(label: String, value: String, options: List<String>,
                     colors: AezoraColorScheme, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall,
            color = colors.onBackground, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.secondary)
        Icon(Icons.Rounded.ChevronRight, null, tint = colors.secondary)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = colors.onSurface) },
                    onClick = { onSelect(opt); expanded = false }
                )
            }
        }
    }
}

@Composable
fun SettingsSliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>,
                      displayValue: String, colors: AezoraColorScheme, onValue: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall,
                color = colors.onBackground, modifier = Modifier.weight(1f))
            Text(displayValue, style = MaterialTheme.typography.bodySmall, color = colors.primary)
        }
        Slider(value = value, onValueChange = onValue, valueRange = range,
            colors = SliderDefaults.colors(thumbColor = colors.primary,
                activeTrackColor = colors.primary, inactiveTrackColor = colors.surfaceVariant))
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String, icon: ImageVector, colors: AezoraColorScheme) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.secondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall,
            color = colors.onBackground, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.secondary)
    }
}

@Composable
fun SettingsActionRow(label: String, icon: ImageVector, colors: AezoraColorScheme, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.error, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = colors.error)
    }
}
