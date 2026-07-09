package com.aezora.next.ui.screens.auth

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.aezora.next.data.db.AezoraDatabase
import com.aezora.next.data.models.MusicService
import com.aezora.next.data.models.ServiceAccount
import com.aezora.next.data.repository.MusicRepository
import com.aezora.next.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ServicesState(
    val accounts: Map<MusicService, ServiceAccount?> = emptyMap(),
    val isSyncing: Boolean = false,
    val message: String? = null
)

class ServicesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))
    private val _state = MutableStateFlow(ServicesState())
    val state = _state.asStateFlow()

    init { loadAccounts() }

    fun loadAccounts() {
        viewModelScope.launch {
            val accounts = MusicService.entries
                .filter { it != MusicService.LOCAL }
                .associateWith { repo.getServiceAccount(it) }
            _state.update { it.copy(accounts = accounts) }
        }
    }

    /**
     * Сохраняет токен → синхронизирует плейлисты автоматически.
     */
    fun connectWithToken(service: MusicService, token: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            val account = ServiceAccount(
                service      = service.name,
                accessToken  = token.trim(),
                isConnected  = true,
                displayName  = service.displayName
            )
            repo.saveServiceAccount(account)

            // Сразу синхронизируем плейлисты
            val count = repo.syncServicePlaylists(service, token.trim())
            loadAccounts()

            val msg = if (count > 0)
                "${service.displayName} подключён! Импортировано плейлистов: $count"
            else
                "${service.displayName} подключён!"
            _state.update { it.copy(isSyncing = false, message = msg) }
        }
    }

    fun disconnect(service: MusicService) {
        viewModelScope.launch {
            repo.disconnectService(service)
            loadAccounts()
            _state.update { it.copy(message = "${service.displayName} отключён") }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@Composable
fun ServicesScreen(vm: ServicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current
    val snackbar = remember { SnackbarHostState() }
    var sheetService by remember { mutableStateOf<MusicService?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .systemBarsPadding()
        ) {
            Text(
                "Сервисы",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onBackground,
                modifier = Modifier.padding(20.dp)
            )

            if (state.isSyncing) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Синхронизация плейлистов...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            val services = MusicService.entries.filter { it != MusicService.LOCAL }
            services.forEach { service ->
                ServiceRow(
                    service  = service,
                    account  = state.accounts[service],
                    colors   = colors,
                    onConnect    = { sheetService = service },
                    onDisconnect = { vm.disconnect(service) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Подсказка по токенам
            TokenHelpCard(colors)
        }
    }

    sheetService?.let { service ->
        TokenInputSheet(
            service  = service,
            colors   = colors,
            onDismiss = { sheetService = null },
            onSave    = { token ->
                vm.connectWithToken(service, token)
                sheetService = null
            }
        )
    }
}

@Composable
fun ServiceRow(
    service: MusicService,
    account: ServiceAccount?,
    colors: AezoraColorScheme,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val connected = account?.isConnected == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Логотип сервиса — грузим PNG из сети через Coil
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (service.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = service.logoUrl,
                    contentDescription = service.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Icon(Icons.Rounded.MusicNote, null, tint = colors.secondary)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                service.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onBackground
            )
            Text(
                if (connected) account?.displayName?.ifEmpty { "Подключён ✓" } ?: "Подключён ✓"
                else "Не подключён",
                style = MaterialTheme.typography.bodySmall,
                color = if (connected) colors.primary else colors.secondary
            )
        }

        if (connected) {
            OutlinedButton(
                onClick = onDisconnect,
                border = BorderStroke(1.dp, colors.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Выйти", color = colors.secondary, style = MaterialTheme.typography.labelMedium)
            }
        } else {
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Войти", color = colors.onPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenInputSheet(
    service: MusicService,
    colors: AezoraColorScheme,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "Вход в ${service.displayName}",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tokenHint(service),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondary
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("OAuth / Bearer токен", color = colors.secondary) },
                visualTransformation = if (visible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            null, tint = colors.secondary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.primary,
                    unfocusedBorderColor = colors.surfaceVariant,
                    focusedTextColor     = colors.onBackground,
                    unfocusedTextColor   = colors.onBackground,
                    cursorColor          = colors.primary
                ),
                maxLines = 4,
                singleLine = false
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { if (token.isNotBlank()) onSave(token) },
                enabled = token.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.Check, null, tint = colors.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Подключить и синхронизировать",
                    color = colors.onPrimary
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun TokenHelpCard(colors: AezoraColorScheme) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Help, null, tint = colors.secondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Где взять токен?", style = MaterialTheme.typography.titleSmall, color = colors.onBackground)
        }
        Spacer(Modifier.height(10.dp))
        TokenHelpItem("SoundCloud", "Авторизуйтесь на soundcloud.com → DevTools → Application → Cookies → oauth_token", colors)
        Spacer(Modifier.height(6.dp))
        TokenHelpItem("VK Музыка", "Используйте Kate Mobile или VK Admin: Settings → Access Token", colors)
        Spacer(Modifier.height(6.dp))
        TokenHelpItem("Яндекс Музыка", "Перейдите на oauth.yandex.ru → выдайте токен приложению music.yandex.ru", colors)
    }
}

@Composable
fun TokenHelpItem(service: String, hint: String, colors: AezoraColorScheme) {
    Column {
        Text("• $service", style = MaterialTheme.typography.labelMedium, color = colors.primary)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.secondary)
    }
}

fun tokenHint(service: MusicService): String = when (service) {
    MusicService.SOUNDCLOUD ->
        "Войдите на soundcloud.com → F12 → Application → Cookies → скопируйте значение oauth_token"
    MusicService.VK ->
        "Используйте приложение Kate Mobile или официальный VK: Настройки → Безопасность → Активные сессии → скопируйте access_token"
    MusicService.YANDEX ->
        "Перейдите на oauth.yandex.ru, создайте приложение или используйте токен из музыкального клиента Яндекса"
    MusicService.YOUTUBE ->
        "Используйте Google OAuth Playground: oauth2.googleapis.com/token с scope=youtube"
    else -> "Введите ваш API-токен"
}
