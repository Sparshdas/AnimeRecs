package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.MediaTypeSelection
import com.example.ui.AnimeRecsViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.MediaCard
import com.example.ui.theme.*

private val GENRES = listOf(
    "All", "Action", "Adventure", "Comedy", "Drama", "Fantasy",
    "Horror", "Mystery", "Psychological", "Romance", "Sci-Fi",
    "Slice of Life", "Sports", "Supernatural", "Thriller"
)

private val FORMATS_ANIME = listOf("All", "TV", "MOVIE", "TV_SHORT", "OVA", "ONA", "SPECIAL")
private val FORMATS_MANGA = listOf("All", "MANGA", "NOVEL", "ONE_SHOT")

private val YEARS = listOf("All", "2025", "2024", "2023", "2022", "2020", "2015", "2010", "2000")
private val STATUSES = listOf("All", "FINISHED", "RELEASING", "NOT_YET_RELEASED")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoveryScreen(
    viewModel: AnimeRecsViewModel,
    modifier: Modifier = Modifier
) {
    val filter by viewModel.discoveryFilter.collectAsState()
    val discoveredList by viewModel.discoveredList.collectAsState()
    val featuredItem by viewModel.featuredRandomItem.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val error by viewModel.discoveryError.collectAsState()

    val revealedAdultIds by viewModel.preferences.revealedAdultMediaIds.collectAsState()
    var showFilterPanel by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "Random Discovery",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Filter by genre, format, year, or status",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = if (showFilterPanel) AnimeViolet else DarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (showFilterPanel) AnimeViolet else DarkBorder),
                modifier = Modifier
                    .testTag("toggle_filters_button")
                    .clickable { showFilterPanel = !showFilterPanel }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = if (showFilterPanel) Color.White else AnimeVioletLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showFilterPanel) "Hide" else "Filters",
                        color = if (showFilterPanel) Color.White else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filter Options Dropdown / Collapsible Drawer
        AnimatedVisibility(visible = showFilterPanel) {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(0.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Media Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MediaTypeSelection.entries.forEach { type ->
                            val isSelected = filter.mediaType == type
                            FilterChip(
                                label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = isSelected,
                                onClick = { viewModel.updateDiscoveryFilter(mediaType = type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 2. Format
                    val formatList = if (filter.mediaType == MediaTypeSelection.ANIME) FORMATS_ANIME else FORMATS_MANGA
                    Column {
                        Text(text = "Format", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(formatList) { fmt ->
                                val isSelected = (filter.format ?: "All") == fmt
                                FilterChip(
                                    label = fmt.replace("_", " "),
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.updateDiscoveryFilter(format = if (fmt == "All") null else fmt)
                                    }
                                )
                            }
                        }
                    }

                    // 3. Year
                    Column {
                        Text(text = "Release Year", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(YEARS) { yr ->
                                val isSelected = if (yr == "All") filter.year == null else filter.year == yr.toIntOrNull()
                                FilterChip(
                                    label = yr,
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.updateDiscoveryFilter(year = yr.toIntOrNull())
                                    }
                                )
                            }
                        }
                    }

                    // 4. Status
                    Column {
                        Text(text = "Status", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(STATUSES) { st ->
                                val isSelected = (filter.status ?: "All") == st
                                FilterChip(
                                    label = st.replace("_", " "),
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.updateDiscoveryFilter(status = if (st == "All") null else st)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Genre Quick-Filter Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated.copy(alpha = 0.5f))
        ) {
            items(GENRES) { genre ->
                val isSelected = (filter.genre ?: "All") == genre
                Surface(
                    color = if (isSelected) AnimeViolet else DarkSurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AnimeViolet else DarkBorderSubtle
                    ),
                    modifier = Modifier
                        .testTag("genre_chip_$genre")
                        .clickable {
                            viewModel.updateDiscoveryFilter(genre = if (genre == "All") null else genre)
                        }
                ) {
                    Text(
                        text = genre,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDiscovering) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = AnimeViolet)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rolling random discovery...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else if (error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(text = "⚠️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error ?: "Discovery failed", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.performDiscovery() }) {
                        Text("Try Again")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Featured Spotlight Card
                    if (featuredItem != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SpotlightCard(
                                media = featuredItem!!,
                                onRollAgain = { viewModel.shuffleDiscovery() },
                                onCardClick = { viewModel.navigateTo(ScreenDestination.Detail(featuredItem!!.id)) }
                            )
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
                            Text(
                                text = "Discovered Titles (${discoveredList.size})",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.shuffleDiscovery() }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Shuffle",
                                    tint = AnimeCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Shuffle", color = AnimeCyan, fontSize = 12.sp)
                            }
                        }
                    }

                    items(discoveredList, key = { it.id }) { item ->
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

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) AnimeViolet else DarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AnimeViolet else DarkBorder),
        modifier = modifier
            .testTag("filter_chip_$label")
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SpotlightCard(
    media: MediaItem,
    onRollAgain: () -> Unit,
    onCardClick: () -> Unit
) {
    Surface(
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.5f)),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spotlight_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AnimeVioletDark.copy(alpha = 0.25f),
                            DarkSurfaceCard
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎲", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FEATURED RANDOM SPOTLIGHT",
                        color = AnimeCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                Button(
                    onClick = onRollAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeViolet),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("roll_again_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Roll", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCardClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                ) {
                    AsyncImage(
                        model = media.coverImage?.bestUrl,
                        contentDescription = media.displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.displayTitle,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${media.displayFormat} • ${media.startDate?.year ?: "TBA"}",
                        color = AnimeCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = media.cleanDescription,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ Tap for similarity recommendations →",
                        color = AnimePink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
