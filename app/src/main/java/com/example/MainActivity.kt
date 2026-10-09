package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AnimeRecsViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AgeConfirmationDialog
import com.example.ui.components.OAuthLoginDialog
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.DiscoveryScreen
import com.example.ui.screens.MyListScreen
import com.example.ui.screens.SearchRecsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AnimeViolet
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeRecsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.data?.toString()?.let { uriString ->
            if (uriString.contains("access_token=") || uriString.startsWith("http://localhost:3000")) {
                viewModel.handleOAuthRedirectUrl(uriString)
            }
        }
        setContent {
            MyApplicationTheme {
                AnimeRecsApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.data?.toString()?.let { uriString ->
            if (uriString.contains("access_token=") || uriString.startsWith("http://localhost:3000")) {
                viewModel.handleOAuthRedirectUrl(uriString)
            }
        }
    }
}

@Composable
fun AnimeRecsApp(viewModel: AnimeRecsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()
    val showAgeDialog by viewModel.showAgeConfirmDialog.collectAsState()
    val showOAuthDialog by viewModel.showOAuthDialog.collectAsState()

    BackHandler(enabled = canNavigateBack || currentScreen is ScreenDestination.Detail) {
        viewModel.navigateBack()
    }

    Scaffold(
        bottomBar = {
            if (currentScreen !is ScreenDestination.Detail) {
                AnimeRecsBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetDestination ->
                when (targetDestination) {
                    is ScreenDestination.SearchRecs -> SearchRecsScreen(viewModel = viewModel)
                    is ScreenDestination.Discovery -> DiscoveryScreen(viewModel = viewModel)
                    is ScreenDestination.Detail -> DetailScreen(
                        viewModel = viewModel,
                        mediaId = targetDestination.mediaId
                    )
                    is ScreenDestination.MyList -> MyListScreen(viewModel = viewModel)
                    is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showAgeDialog) {
        AgeConfirmationDialog(
            onConfirm = { viewModel.confirmAgeAndEnableAdult() },
            onDismiss = { viewModel.dismissAgeDialog() }
        )
    }

    if (showOAuthDialog) {
        OAuthLoginDialog(
            authUrl = viewModel.aniListAuthUrl,
            onTokenExtracted = { tokenOrUrl ->
                viewModel.handleOAuthRedirectUrl(tokenOrUrl)
            },
            onDismiss = { viewModel.dismissAniListAuth() }
        )
    }
}

@Composable
fun AnimeRecsBottomNav(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = TextPrimary,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        // Tab 1: Recommendations & Search
        val isSearch = currentScreen is ScreenDestination.SearchRecs
        NavigationBarItem(
            selected = isSearch,
            onClick = { onNavigate(ScreenDestination.SearchRecs) },
            icon = {
                Icon(
                    imageVector = if (isSearch) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = "Recommendations",
                    tint = if (isSearch) Color.White else TextSecondary
                )
            },
            label = { Text("Recs", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AnimeViolet,
                selectedTextColor = Color.White,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_search")
        )

        // Tab 2: Random Discovery
        val isDiscovery = currentScreen is ScreenDestination.Discovery
        NavigationBarItem(
            selected = isDiscovery,
            onClick = { onNavigate(ScreenDestination.Discovery) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Discovery",
                    tint = if (isDiscovery) Color.White else TextSecondary
                )
            },
            label = { Text("Discover", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AnimeViolet,
                selectedTextColor = Color.White,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_discovery")
        )

        // Tab 3: My AniList Library
        val isMyList = currentScreen is ScreenDestination.MyList
        NavigationBarItem(
            selected = isMyList,
            onClick = { onNavigate(ScreenDestination.MyList) },
            icon = {
                Icon(
                    imageVector = if (isMyList) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "My List",
                    tint = if (isMyList) Color.White else TextSecondary
                )
            },
            label = { Text("My List", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AnimeViolet,
                selectedTextColor = Color.White,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_my_list")
        )

        // Tab 4: Settings & 18+ Control
        val isSettings = currentScreen is ScreenDestination.Settings
        NavigationBarItem(
            selected = isSettings,
            onClick = { onNavigate(ScreenDestination.Settings) },
            icon = {
                Icon(
                    imageVector = if (isSettings) Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = if (isSettings) Color.White else TextSecondary
                )
            },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AnimeViolet,
                selectedTextColor = Color.White,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}


