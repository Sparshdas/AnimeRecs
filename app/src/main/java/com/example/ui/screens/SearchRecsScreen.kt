package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaTypeSelection
import com.example.ui.AnimeRecsViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AccountSwitcherDropdown
import com.example.ui.components.MediaCard
import com.example.ui.theme.*

@Composable
fun SearchRecsScreen(
    viewModel: AnimeRecsViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val trendingMedia by viewModel.trendingMedia.collectAsState()
    val isLoadingTrending by viewModel.isLoadingTrending.collectAsState()

    val viewer by viewModel.viewer.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeViewerId by viewModel.activeViewerId.collectAsState()
    val personalizedRecommendations by viewModel.personalizedRecommendations.collectAsState()
    val isLoadingPersonalized by viewModel.isLoadingPersonalized.collectAsState()
    val userAffinityGenres by viewModel.userAffinityGenres.collectAsState()

    val revealedAdultIds by viewModel.preferences.revealedAdultMediaIds.collectAsState()

    val savedIndex by viewModel.searchScrollIndex.collectAsState()
    val savedOffset by viewModel.searchScrollOffset.collectAsState()
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = savedIndex,
        initialFirstVisibleItemScrollOffset = savedOffset
    )

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> viewModel.saveSearchScrollPosition(index, offset) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header & Branding with Account Switcher
        HeaderSection(
            selectedMediaType = selectedMediaType,
            onSelectMediaType = { viewModel.setMediaType(it) },
            viewer = viewer,
            accounts = accounts,
            activeViewerId = activeViewerId,
            onSwitchAccount = { viewModel.switchAccount(it) },
            onAddNewAccount = { viewModel.openAniListAuth() },
            onRemoveAccount = { viewModel.removeAccount(it) }
        )

        // Search Bar Input
        SearchBarSection(
            query = searchQuery,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            selectedMediaType = selectedMediaType
        )

        // Results or Trending Feed
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSearching) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        color = AnimeViolet,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Searching AniList ${selectedMediaType.name.lowercase()}...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else if (searchQuery.isNotBlank()) {
                // Search Results Grid
                if (searchResults.isEmpty() && searchError == null) {
                    EmptySearchState(query = searchQuery, mediaType = selectedMediaType)
                } else if (searchError != null) {
                    ErrorState(message = searchError ?: "Search failed")
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_grid")
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${searchResults.size} titles found for \"$searchQuery\"",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tap to view recommendations",
                                    color = AnimeCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        items(searchResults, key = { it.id }) { item ->
                            MediaCard(
                                media = item,
                                isAdultRevealed = revealedAdultIds.contains(item.id),
                                onRevealAdult = { viewModel.revealAdultContent(item.id) },
                                onClick = { viewModel.navigateTo(ScreenDestination.Detail(item.id)) }
                            )
                        }
                    }
                }
            } else {
                // When no search active: Show Personalization Shelf + Trending / Popular
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("trending_grid")
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HeroPromoBanner(
                            selectedMediaType = selectedMediaType,
                            onDiscoverClick = { viewModel.navigateTo(ScreenDestination.Discovery) }
                        )
                    }

                    // --- "Based on your list" Section ---
                    if (viewer != null && personalizedRecommendations.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AnimeCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Based on Your List",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "@${viewer?.name}'s Taste",
                                        color = AnimeCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (userAffinityGenres.isNotEmpty()) {
                                    Text(
                                        text = "Personalized from your 8+ rated favorites in ${userAffinityGenres.take(3).joinToString(", ")}",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                    )
                                }
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(personalizedRecommendations.size) { index ->
                                        val item = personalizedRecommendations[index]
                                        Box(modifier = Modifier.width(140.dp)) {
                                            MediaCard(
                                                media = item,
                                                isAdultRevealed = revealedAdultIds.contains(item.id),
                                                onRevealAdult = { viewModel.revealAdultContent(item.id) },
                                                onClick = { viewModel.navigateTo(ScreenDestination.Detail(item.id)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (viewer == null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Surface(
                                color = DarkSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { viewModel.openAniListAuth() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AnimePink,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Sync AniList for Personal Recommendations",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Boost recommendations based on your 8+ rated titles",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AnimeViolet)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Connect", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = AnimePink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trending ${selectedMediaType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Select any for recommendations",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isLoadingTrending) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                CircularProgressIndicator(color = AnimeViolet)
                            }
                        }
                    } else {
                        items(trendingMedia, key = { it.id }) { item ->
                            MediaCard(
                                media = item,
                                isAdultRevealed = revealedAdultIds.contains(item.id),
                                onRevealAdult = { viewModel.revealAdultContent(item.id) },
                                onClick = { viewModel.navigateTo(ScreenDestination.Detail(item.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    selectedMediaType: MediaTypeSelection,
    onSelectMediaType: (MediaTypeSelection) -> Unit,
    viewer: com.example.data.model.AniListViewer?,
    accounts: List<com.example.data.model.AniListAccount>,
    activeViewerId: Int?,
    onSwitchAccount: (Int) -> Unit,
    onAddNewAccount: () -> Unit,
    onRemoveAccount: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Surface(
                    color = AnimeViolet.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.6f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AnimeViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AnimeRecs",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "AniList Recs Engine",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Multi-Account Switcher
            AccountSwitcherDropdown(
                viewer = viewer,
                accounts = accounts,
                activeViewerId = activeViewerId,
                onSwitchAccount = onSwitchAccount,
                onAddNewAccount = onAddNewAccount,
                onRemoveAccount = onRemoveAccount
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Media Type Selector: Anime | Manga | Manhwa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MediaTypeTab(
                title = "Anime",
                icon = "🎌",
                isSelected = selectedMediaType == MediaTypeSelection.ANIME,
                onClick = { onSelectMediaType(MediaTypeSelection.ANIME) },
                modifier = Modifier.weight(1f)
            )
            MediaTypeTab(
                title = "Manga",
                icon = "📖",
                isSelected = selectedMediaType == MediaTypeSelection.MANGA,
                onClick = { onSelectMediaType(MediaTypeSelection.MANGA) },
                modifier = Modifier.weight(1f)
            )
            MediaTypeTab(
                title = "Manhwa",
                icon = "🇰🇷",
                isSelected = selectedMediaType == MediaTypeSelection.MANHWA,
                onClick = { onSelectMediaType(MediaTypeSelection.MANHWA) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MediaTypeTab(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) AnimeViolet else Color.Transparent
    val textCol = if (isSelected) Color.White else TextSecondary

    Surface(
        color = bg,
        shape = RoundedCornerShape(9.dp),
        modifier = modifier
            .testTag("media_type_tab_${title.lowercase()}")
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = textCol,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChanged: (String) -> Unit,
    selectedMediaType: MediaTypeSelection
) {
    val placeholder = when (selectedMediaType) {
        MediaTypeSelection.ANIME -> "Search anime (e.g. Frieren, Steins;Gate)..."
        MediaTypeSelection.MANGA -> "Search manga (e.g. Berserk, Chainsaw Man)..."
        MediaTypeSelection.MANHWA -> "Search manhwa (e.g. Solo Leveling, Omniscient)..."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text(text = placeholder, color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AnimeVioletLight
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated,
                focusedBorderColor = AnimeViolet,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input")
        )
    }
}

@Composable
private fun HeroPromoBanner(
    selectedMediaType: MediaTypeSelection,
    onDiscoverClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AnimeVioletDark.copy(alpha = 0.35f),
                            Color(0xFF0F172A).copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Recommendation Engine",
                            color = AnimeCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Find your next favorite ${selectedMediaType.name.lowercase()}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search any title to get 90%+ match similarity recommendations calculated from genres, themes, and community signal.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        color = AnimeViolet,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onDiscoverClick)
                            .testTag("hero_discover_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Discover",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String, mediaType: MediaTypeSelection) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(text = "🔍", fontSize = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No results for \"$query\"",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try searching by romaji or English title in ${mediaType.name.lowercase()}.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(text = "⚠️", fontSize = 38.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
