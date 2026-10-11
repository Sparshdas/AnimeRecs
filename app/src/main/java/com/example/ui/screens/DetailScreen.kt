package com.example.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.ScoredRecommendation
import com.example.ui.AnimeRecsViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AdultBadge
import com.example.ui.components.FullscreenImageViewer
import com.example.ui.components.SimilarityBadge
import com.example.ui.components.SimilarityBreakdownDialog
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimePink
import com.example.ui.theme.AnimeViolet
import com.example.ui.theme.AnimeVioletLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAdult
import com.example.ui.theme.ScoreGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    viewModel: AnimeRecsViewModel,
    modifier: Modifier = Modifier,
    mediaId: Int? = null
) {
    val detailMedia by viewModel.detailMedia.collectAsState()
    val detailRecommendations by viewModel.detailRecommendations.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val detailError by viewModel.detailError.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val targetMediaId = mediaId ?: (currentScreen as? ScreenDestination.Detail)?.mediaId ?: detailMedia?.id
    val cachedPair = targetMediaId?.let { viewModel.getCachedDetail(it) }

    val media = if (detailMedia?.id == targetMediaId) detailMedia else cachedPair?.first
    val recommendations = if (detailMedia?.id == targetMediaId) detailRecommendations else (cachedPair?.second ?: emptyList())
    val isLoading = if (media != null) false else isLoadingDetail
    val error = if (media != null) null else detailError

    val detailUserEntry by viewModel.detailUserEntry.collectAsState()
    val userAnimeList by viewModel.userAnimeList.collectAsState()
    val userMangaList by viewModel.userMangaList.collectAsState()
    val viewer by viewModel.viewer.collectAsState()
    val activeViewerId by viewModel.activeViewerId.collectAsState()

    val savedIds by viewModel.preferences.savedMediaIds.collectAsState()
    val revealedAdultIds by viewModel.preferences.revealedAdultMediaIds.collectAsState()

    val currentUserEntry = remember(media?.id, media?.type, detailUserEntry, userAnimeList, userMangaList) {
        val id = media?.id ?: return@remember null
        if (detailUserEntry?.mediaId == id || detailUserEntry?.media?.id == id) {
            detailUserEntry
        } else {
            val list = if (media.type.equals("MANGA", ignoreCase = true)) userMangaList else userAnimeList
            list.find { it.mediaId == id || it.media?.id == id }
        }
    }

    var isSynopsisExpanded by remember(media?.id) { mutableStateOf(false) }
    var statusDropdownExpanded by remember(media?.id) { mutableStateOf(false) }
    var showConnectPromptDialog by remember { mutableStateOf(false) }
    var fullscreenImageUrl by remember(media?.id) { mutableStateOf<String?>(null) }

    if (showConnectPromptDialog) {
        AlertDialog(
            onDismissRequest = { showConnectPromptDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Connect to AniList",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Connect your AniList account first to add titles and sync your watch/read status.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConnectPromptDialog = false
                        viewModel.openAniListAuth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeViolet),
                    modifier = Modifier.testTag("detail_connect_anilist_confirm_button")
                ) {
                    Text("Connect AniList", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConnectPromptDialog = false }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = media?.displayTitle ?: "Title Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (media != null) {
                        val isManga = media.type.equals("MANGA", ignoreCase = true)
                        val currentStatusEnum = currentUserEntry?.status?.uppercase()
                        val friendlyStatus = formatStatusLabel(currentStatusEnum, isManga)
                        val isConnected = viewer != null || activeViewerId != null

                        Box {
                            Surface(
                                onClick = {
                                    if (!isConnected) {
                                        showConnectPromptDialog = true
                                    } else {
                                        statusDropdownExpanded = true
                                    }
                                },
                                color = if (friendlyStatus != null) AnimeViolet else AnimeViolet.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (friendlyStatus != null) AnimeViolet else AnimeViolet.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.testTag("detail_status_button")
                            ) {
                                Text(
                                    text = if (friendlyStatus != null) "$friendlyStatus ▾" else "+ Add to List",
                                    color = if (friendlyStatus != null) Color.White else AnimeVioletLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false },
                                modifier = Modifier
                                    .background(DarkSurfaceCard)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            ) {
                                getStatusOptions(isManga).forEach { (enumValue, label) ->
                                    val isSelected = currentStatusEnum == enumValue
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = AnimeCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.size(16.dp))
                                                }
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) AnimeCyan else TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        },
                                        onClick = {
                                            statusDropdownExpanded = false
                                            viewModel.setMediaListStatus(media.id, enumValue)
                                        },
                                        modifier = Modifier.testTag("detail_status_option_${enumValue.lowercase()}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        val isSaved = savedIds.contains(media.id)
                        IconButton(
                            onClick = { viewModel.toggleSaveCurrentMedia(media.id) },
                            modifier = Modifier.testTag("detail_save_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Saved" else "Save",
                                tint = if (isSaved) AnimePink else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = AnimeViolet)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading media & computing recommendations...",
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
                    Text(text = "⚠️", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = error,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (media != null) {
                val currentMedia = media
                val isAdult = currentMedia.isAdult == true
                val isAdultRevealed = revealedAdultIds.contains(currentMedia.id)
                val shouldBlur = isAdult && !isAdultRevealed

                val (savedIndex, savedOffset) = remember(currentMedia.id) {
                    viewModel.getDetailScrollPosition(currentMedia.id)
                }
                val listState = remember(currentMedia.id) {
                    LazyListState(
                        firstVisibleItemIndex = savedIndex,
                        firstVisibleItemScrollOffset = savedOffset
                    )
                }

                LaunchedEffect(listState, currentMedia.id) {
                    snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                        .collect { (index, offset) ->
                            viewModel.saveDetailScrollPosition(currentMedia.id, index, offset)
                        }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("detail_scroll_view")
                ) {
                    // Hero Banner / Top Header
                    item {
                        DetailHeroSection(
                            media = currentMedia,
                            shouldBlur = shouldBlur,
                            onRevealAdult = { viewModel.revealAdultContent(currentMedia.id) },
                            onCoverClick = { url -> fullscreenImageUrl = url }
                        )
                    }

                    // Metadata Badges (Score, Episodes, Status, Studio)
                    item {
                        DetailMetadataGrid(media = currentMedia)
                    }

                    // Genres & Tags
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Genres & Themes",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                currentMedia.genres.forEach { genre ->
                                    Surface(
                                        color = AnimeViolet.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = genre,
                                            color = AnimeVioletLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                currentMedia.tags.take(6).forEach { tag ->
                                    Surface(
                                        color = DarkSurfaceElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
                                    ) {
                                        Text(
                                            text = "#${tag.name}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Synopsis
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Synopsis",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentMedia.cleanDescription,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .animateContentSize()
                                    .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSynopsisExpanded) "Show less ▲" else "Read more ▼",
                                color = AnimeCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Recommendations Section Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Ranked Recommendations",
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ranked by genres (35%), tags (30%), votes (20%), format (15%)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    if (recommendations.isEmpty()) {
                        item {
                            Surface(
                                color = DarkSurfaceCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "No direct recommendations found for this title yet.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(recommendations, key = { it.media.id }) { rec ->
                            RecommendationRowItem(
                                scoredRec = rec,
                                isAdultRevealed = revealedAdultIds.contains(rec.media.id),
                                onRevealAdult = { viewModel.revealAdultContent(rec.media.id) },
                                onClick = { viewModel.navigateTo(ScreenDestination.Detail(rec.media.id)) }
                            )
                        }
                    }
                }
            }
        }
        }

        fullscreenImageUrl?.let { url ->
            FullscreenImageViewer(
                imageUrl = url,
                onDismiss = { fullscreenImageUrl = null }
            )
        }
    }
}

@Composable
private fun DetailHeroSection(
    media: MediaItem,
    shouldBlur: Boolean,
    onRevealAdult: () -> Unit,
    onCoverClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Banner Image or Blurred Cover as Backdrop
        val backdropUrl = media.bannerImage ?: media.coverImage?.bestUrl
        AsyncImage(
            model = backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(16.dp)
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            DarkSurface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Main Poster & Title Row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Poster Card
            val fullResCoverUrl = media.coverImage?.extraLarge
                ?: media.coverImage?.large
                ?: media.coverImage?.bestUrl
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AnimeViolet.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .clickable(enabled = !shouldBlur && !fullResCoverUrl.isNullOrBlank()) {
                        fullResCoverUrl?.let(onCoverClick)
                    }
                    .testTag("detail_cover_thumbnail")
            ) {
                AsyncImage(
                    model = media.coverImage?.bestUrl,
                    contentDescription = media.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (shouldBlur) Modifier.blur(20.dp) else Modifier)
                )

                if (media.isAdult == true) {
                    AdultBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }

                if (shouldBlur) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onRevealAdult() }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RedAdult,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "18+ Reveal",
                                color = RedAdult,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Core info
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = AnimeViolet,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = media.displayFormat,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = media.displayTitle,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                if (media.title?.native != null) {
                    Text(
                        text = media.title.native,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = media.displayStudioOrAuthor,
                    color = AnimeCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DetailMetadataGrid(media: MediaItem) {
    Surface(
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score
            MetaPill(
                label = "Score",
                value = if (media.averageScore != null && media.averageScore > 0) "${media.averageScore}%" else "N/A",
                color = ScoreGold
            )

            MetaDivider()

            // Episodes or Chapters
            val length = when {
                media.episodes != null -> "${media.episodes} eps"
                media.chapters != null -> "${media.chapters} chs"
                else -> media.status?.replace("_", " ") ?: "Unknown"
            }
            MetaPill(
                label = if (media.episodes != null) "Episodes" else "Length",
                value = length,
                color = AnimeCyan
            )

            MetaDivider()

            // Status / Year
            val year = media.startDate?.year ?: media.seasonYear
            MetaPill(
                label = "Released",
                value = if (year != null) "$year" else "TBA",
                color = AnimePink
            )

            MetaDivider()

            // Country / Origin
            val origin = when (media.countryOfOrigin) {
                "KR" -> "Korea (KR)"
                "JP" -> "Japan (JP)"
                "CN" -> "China (CN)"
                else -> "Global"
            }
            MetaPill(
                label = "Origin",
                value = origin,
                color = AnimeVioletLight
            )
        }
    }
}

@Composable
private fun MetaPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun MetaDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(DarkBorder)
    )
}

@Composable
private fun RecommendationRowItem(
    scoredRec: ScoredRecommendation,
    isAdultRevealed: Boolean,
    onRevealAdult: () -> Unit,
    onClick: () -> Unit
) {
    val media = scoredRec.media
    val score = scoredRec.score
    var showBreakdown by remember { mutableStateOf(false) }

    val isAdult = media.isAdult == true
    val shouldBlur = isAdult && !isAdultRevealed

    Surface(
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("recommendation_item_${media.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
            ) {
                AsyncImage(
                    model = media.coverImage?.bestUrl,
                    contentDescription = media.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (shouldBlur) Modifier.blur(18.dp) else Modifier)
                )

                if (isAdult) {
                    AdultBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                    )
                }

                if (shouldBlur) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onRevealAdult() }
                    ) {
                        Text(
                            text = "18+ View",
                            color = RedAdult,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SimilarityBadge(
                        score = score,
                        onClick = { showBreakdown = true }
                    )

                    if (media.averageScore != null && media.averageScore > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ScoreGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${media.averageScore}%",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = media.displayTitle,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = media.genres.take(3).joinToString(" • "),
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${media.displayFormat} • ${media.startDate?.year ?: "TBA"}",
                        color = AnimeCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Why this match? ℹ️",
                        color = AnimeVioletLight,
                        fontSize = 10.sp,
                        modifier = Modifier.clickable { showBreakdown = true }
                    )
                }
            }
        }
    }

    if (showBreakdown) {
        SimilarityBreakdownDialog(
            title = media.displayTitle,
            score = score,
            onDismiss = { showBreakdown = false }
        )
    }
}

private fun getStatusOptions(isManga: Boolean): List<Pair<String, String>> = listOf(
    "CURRENT" to "Watching",
    "PLANNING" to "Planning",
    "COMPLETED" to "Completed",
    "PAUSED" to "On Hold",
    "DROPPED" to "Dropped",
    "REPEATING" to if (isManga) "Rereading" else "Rewatching"
)

private fun formatStatusLabel(status: String?, isManga: Boolean): String? {
    if (status.isNullOrBlank()) return null
    return when (status.uppercase()) {
        "CURRENT" -> "Watching"
        "PLANNING" -> "Planning"
        "COMPLETED" -> "Completed"
        "PAUSED" -> "On Hold"
        "DROPPED" -> "Dropped"
        "REPEATING" -> if (isManga) "Rereading" else "Rewatching"
        else -> status.lowercase().replaceFirstChar { it.uppercase() }
    }
}

