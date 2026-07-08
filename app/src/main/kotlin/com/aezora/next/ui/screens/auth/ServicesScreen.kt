package com.aezora.next.ui.screens.auth

import android.app.Application
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
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

data class AuthState(
    val accounts: Map<MusicService, ServiceAccount?> = emptyMap(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(AezoraDatabase.getInstance(app))
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init { loadAccounts() }

    fun loadAccounts() {
        viewModelScope.launch {
            val accounts = MusicService.entries.associateWith {
                repo.getServiceAccount(it)
            }
            _state.update { it.copy(accounts = accounts) }
        }
    }

    fun saveToken(service: MusicService, token: String, isOAuth: Boolean = false) {
        viewModelScope.launch {
            val account = ServiceAccount(
                service = service.name,
                accessToken = if (isOAuth) token else "",
                refreshToken = if (!isOAuth) token else "",
                isConnected = true,
                displayName = service.displayName
            )
            repo.saveServiceAccount(account)
            loadAccounts()
            _state.update { it.copy(message = "${service.displayName} подключён!") }
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

// OAuth URLs
fun getOAuthUrl(service: MusicService): String = when (service) {
    MusicService.SOUNDCLOUD ->
        "https://soundcloud.com/connect?client_id=yNSW5UvBmb1A5j7qPUtIMuB9Itx3jsOC" +
        "&redirect_uri=aezora://callback&response_type=token&scope=non-expiring"
    MusicService.VK ->
        "https://oauth.vk.com/authorize?client_id=2685278&scope=audio,offline" +
        "&redirect_uri=https://oauth.vk.com/blank.html&display=mobile&response_type=token"
    MusicService.YANDEX ->
        "https://oauth.yandex.ru/authorize?response_type=token&client_id=23cabbbdc6cd418abb4b39c32c41195d"
    MusicService.YOUTUBE ->
        "https://accounts.google.com/o/oauth2/v2/auth?client_id=YOUR_CLIENT_ID" +
        "&redirect_uri=aezora://callback&response_type=token&scope=https://www.googleapis.com/auth/youtube"
    MusicService.LOCAL -> ""
}

@Composable
fun ServicesScreen(vm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by vm.state.collectAsState()
    val colors = LocalAezoraColors.current
    var selectedService by remember { mutableStateOf<MusicService?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .systemBarsPadding()
        ) {
            Text(
                "Сервисы",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onBackground,
                modifier = Modifier.padding(20.dp)
            )

            val services = MusicService.entries.filter { it != MusicService.LOCAL }
            services.forEach { service ->
                val account = state.accounts[service]
                ServiceRow(
                    service = service,
                    account = account,
                    colors = colors,
                    onConnect = { selectedService = service },
                    onDisconnect = { vm.disconnect(service) }
                )
            }
        }
    }

    // Service login sheet
    selectedService?.let { service ->
        ServiceLoginSheet(
            service = service,
            colors = colors,
            onDismiss = { selectedService = null },
            onTokenSaved = { token, isOAuth ->
                vm.saveToken(service, token, isOAuth)
                selectedService = null
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Service logo
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = service.logoUrl,
                contentDescription = service.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(service.displayName, style = MaterialTheme.typography.titleSmall,
                color = colors.onBackground)
            Text(
                if (connected) account?.displayName?.ifEmpty { "Подключён" } ?: "Подключён"
                else "Не подключён",
                style = MaterialTheme.typography.bodySmall,
                color = if (connected) colors.primary else colors.secondary
            )
        }
        if (connected) {
            OutlinedButton(
                onClick = onDisconnect,
                border = BorderStroke(1.dp, colors.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Отключить", color = colors.secondary,
                    style = MaterialTheme.typography.labelMedium)
            }
        } else {
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Войти", color = colors.onPrimary,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceLoginSheet(
    service: MusicService,
    colors: AezoraColorScheme,
    onDismiss: () -> Unit,
    onTokenSaved: (String, Boolean) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) } // 0=Web, 1=Token
    var manualToken by remember { mutableStateOf("") }
    var tokenVisible by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "Вход в ${service.displayName}",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground
            )
            Spacer(Modifier.height(16.dp))

            // Tabs: Web / Токен
            TabRow(
                selectedTabIndex = tab,
                containerColor = colors.surfaceVariant,
                contentColor = colors.primary,
                indicator = {}
            ) {
                listOf("Web вход", "Токен вручную").forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (tab == i) colors.primary else Color.Transparent)
                    ) {
                        Text(
                            title, color = if (tab == i) colors.onPrimary else colors.secondary,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            when (tab) {
                0 -> {
                    // Web OAuth login
                    if (showWebView) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(480.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AndroidView(factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView, url: String
                                        ): Boolean {
                                            // Catch OAuth callback
                                            if (url.startsWith("aezora://callback") ||
                                                url.contains("access_token=")) {
                                                val token = extractToken(url)
                                                if (token.isNotEmpty()) {
                                                    onTokenSaved(token, true)
                                                }
                                                return true
                                            }
                                            // VK specific
                                            if (url.contains("blank.html") && url.contains("access_token")) {
                                                val token = extractToken(url)
                                                if (token.isNotEmpty()) onTokenSaved(token, true)
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                    loadUrl(getOAuthUrl(service))
                                }
                            }, modifier = Modifier.fillMaxSize())
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.Language, null,
                                tint = colors.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Откроется браузер для входа в ${service.displayName}.\n" +
                                "После входа токен сохранится автоматически.",
                                color = colors.secondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { showWebView = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.OpenInBrowser, null,
                                    tint = colors.onPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("Войти через браузер", color = colors.onPrimary)
                            }
                        }
                    }
                }
                1 -> {
                    // Manual token
                    Text(
                        "Вставьте OAuth или Bearer токен от ${service.displayName}",
                        color = colors.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualToken,
                        onValueChange = { manualToken = it },
                        label = { Text("Токен", color = colors.secondary) },
                        visualTransformation = if (tokenVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                Icon(
                                    if (tokenVisible) Icons.Rounded.Visibility
                                    else Icons.Rounded.VisibilityOff,
                                    null, tint = colors.secondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.surfaceVariant,
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground
                        ),
                        maxLines = 3
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (manualToken.isNotBlank()) onTokenSaved(manualToken.trim(), false)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = manualToken.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Сохранить токен", color = colors.onPrimary)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun extractToken(url: String): String {
    return try {
        val fragment = if (url.contains("#")) url.substringAfter("#") else url.substringAfter("?")
        fragment.split("&")
            .firstOrNull { it.startsWith("access_token=") }
            ?.substringAfter("access_token=") ?: ""
    } catch (e: Exception) { "" }
}
