package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.AniListViewer
import com.example.data.model.MediaListEntry
import com.example.data.model.MediaTypeSelection
import com.example.ui.AnimeRecsViewModel
import com.example.ui.MyListSortOption
import com.example.ui.ScreenDestination
import com.example.ui.components.AccountSwitcherDropdown
import com.example.ui.components.MediaTypeToggleBar
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeEmerald
import com.example.ui.theme.AnimePink
import com.example.ui.theme.AnimeViolet
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MyListScreen(viewModel: AnimeRecsViewModel) {
    val viewer by viewModel.viewer.collectAsState()
    val isLoadingViewer by viewModel.isLoadingViewer.collectAsState()
    val userListEntries by viewModel.userListEntries.collectAsState()
    val isLoadingList by viewModel.isLoadingUserList.collectAsState()
    val selectedStatus by viewModel.selectedUserListStatus.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val userAffinityGenres by viewModel.userAffinityGenres.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeViewerId by viewModel.activeViewerId.collectAsState()
    val selectedSort by viewModel.selectedUserListSort.collectAsState()
    val isGridView by viewModel.isUserListViewGrid.collectAsState()

    val savedIndex by viewModel.myListScrollIndex.collectAsState()
    val savedOffset by viewModel.myListScrollOffset.collectAsState()
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = savedIndex,
        initialFirstVisibleItemScrollOffset = savedOffset
    )

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> viewModel.saveMyListScrollPosition(index, offset) }
    }

    val completedPromptEntry by viewModel.completedPromptEntry.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(completedPromptEntry) {
        val promptEntry = completedPromptEntry ?: return@LaunchedEffect
        val title = promptEntry.media?.displayTitle ?: "Title"
        val result = snackbarHostState.showSnackbar(
            message = "Marked as Completed? ($title)",
            actionLabel = "Mark Complete",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.markEntryCompleted(promptEntry)
        }
        viewModel.clearCompletedPrompt()
    }

    var searchQuery by remember { mutableStateOf("") }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    val statusTabs = listOf("ALL", "CURRENT", "PLANNING", "COMPLETED", "PAUSED", "DROPPED")

    val sortedAndFilteredEntries = remember(userListEntries, selectedStatus, searchQuery, selectedSort) {
        userListEntries
            .filter { entry ->
                val matchesStatus = if (selectedStatus == "ALL") true else {
                    entry.status.equals(selectedStatus, ignoreCase = true)
                }
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    val title = entry.media?.displayTitle?.lowercase() ?: ""
                    title.contains(searchQuery.lowercase().trim())
                }
                matchesStatus && matchesSearch
            }
            .let { list ->
                when (selectedSort) {
                    MyListSortOption.SCORE_HIGH_TO_LOW -> {
                        list.sortedWith(
                            compareByDescending<MediaListEntry> {
                                val s = it.score ?: 0.0
                                if (s > 0.0) 1 else 0
                            }.thenByDescending {
                                it.score ?: 0.0
                            }.thenBy {
                                it.media?.displayTitle?.lowercase() ?: ""
                            }
                        )
                    }
                    MyListSortOption.SCORE_LOW_TO_HIGH -> {
                        list.sortedWith(
                            compareByDescending<MediaListEntry> {
                                val s = it.score ?: 0.0
                                if (s > 0.0) 1 else 0
                            }.thenBy {
                                it.score ?: 0.0
                            }.thenBy {
                                it.media?.displayTitle?.lowercase() ?: ""
                            }
                        )
                    }
                    MyListSortOption.TITLE_A_Z -> {
                        list.sortedBy { it.media?.displayTitle?.lowercase() ?: "" }
                    }
                    MyListSortOption.RECENTLY_UPDATED -> {
                        list.sortedByDescending { it.updatedAt ?: 0L }
                    }
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 145.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
            .testTag("my_list_screen")
    ) {
        // 1. App / Screen Header (Scrollable)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "My AniList Library",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (viewer != null) "Synced with @${viewer?.name}" else "Sync your profile & recs",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AccountSwitcherDropdown(
                        viewer = viewer,
                        accounts = accounts,
                        activeViewerId = activeViewerId,
                        onSwitchAccount = { viewModel.switchAccount(it) },
                        onAddNewAccount = { viewModel.openAniListAuth() },
                        onRemoveAccount = { viewModel.removeAccount(it) }
                    )

                    if (viewer != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.loadUserMediaList() },
                            modifier = Modifier.size(36.dp).testTag("refresh_user_list_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh List",
                                tint = AnimeCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Media Type Selector (Anime / Manga / Manhwa)
        item(span = { GridItemSpan(maxLineSpan) }) {
            MediaTypeToggleBar(
                selectedType = selectedMediaType,
                onTypeSelected = { viewModel.setMediaType(it) },
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (viewer == null) {
            // Not connected view
            if (isLoadingViewer) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AnimeViolet)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Verifying AniList account...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NotConnectedState(
                        onConnectClick = { viewModel.openAniListAuth() }
                    )
                }
            }
        } else {
            // 3. Connected User Header Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                ConnectedUserHeader(
                    viewer = viewer!!,
                    affinityGenres = userAffinityGenres,
                    onDisconnect = { viewModel.disconnectAniList() }
                )
            }

            // 4. Status Filter Tabs (Scrollable along with page)
            item(span = { GridItemSpan(maxLineSpan) }) {
                ScrollableTabRow(
                    selectedTabIndex = statusTabs.indexOf(selectedStatus).coerceAtLeast(0),
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        val index = statusTabs.indexOf(selectedStatus).coerceAtLeast(0)
                        if (index < tabPositions.size) {
                            SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = AnimeViolet,
                                height = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    statusTabs.forEach { status ->
                        val isSelected = selectedStatus == status
                        val displayLabel = when (status) {
                            "CURRENT" -> if (selectedMediaType == MediaTypeSelection.ANIME) "Watching" else "Reading"
                            "PLANNING" -> "Planning"
                            "COMPLETED" -> "Completed"
                            "PAUSED" -> "Paused"
                            "DROPPED" -> "Dropped"
                            else -> "All (${userListEntries.size})"
                        }
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setUserListStatusFilter(status) },
                            text = {
                                Text(
                                    text = displayLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // 5. Quick Search within list
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter your list by title...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AnimeViolet,
                        unfocusedBorderColor = DarkBorderSubtle,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }

            // 6. Sort and View Mode Controls Row
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sortedAndFilteredEntries.size} ${if (sortedAndFilteredEntries.size == 1) "title" else "titles"}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sort Dropdown Button
                        Box {
                            Surface(
                                onClick = { sortDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                                modifier = Modifier.testTag("sort_dropdown_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort Options",
                                        tint = AnimeViolet,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = selectedSort.displayName,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false },
                                modifier = Modifier
                                    .background(DarkSurfaceCard)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                MyListSortOption.entries.forEach { option ->
                                    val isSelected = option == selectedSort
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
                                                    text = option.displayName,
                                                    color = if (isSelected) AnimeCyan else TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.setUserListSort(option)
                                            sortDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        // Grid / List View Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleUserListViewMode() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                                .testTag("toggle_view_mode_button")
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 7. Loading state, Empty state, or List/Grid items
            if (isLoadingList) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AnimeViolet)
                    }
                }
            } else if (sortedAndFilteredEntries.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No entries match \"$searchQuery\"" else "No titles in $selectedStatus",
                                color = TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add anime/manga to your AniList profile to track them here.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                if (isGridView) {
                    items(sortedAndFilteredEntries, key = { it.id }) { entry ->
                        UserMediaGridCard(
                            entry = entry,
                            onIncrementProgress = { delta, total ->
                                viewModel.incrementProgress(entry, delta, total)
                            },
                            onClick = {
                                val mediaId = entry.media?.id ?: entry.mediaId
                                if (mediaId != null) {
                                    viewModel.navigateTo(ScreenDestination.Detail(mediaId))
                                }
                            }
                        )
                    }
                } else {
                    items(
                        sortedAndFilteredEntries,
                        key = { it.id },
                        span = { GridItemSpan(maxLineSpan) }
                    ) { entry ->
                        UserMediaListCard(
                            entry = entry,
                            onIncrementProgress = { delta, total ->
                                viewModel.incrementProgress(entry, delta, total)
                            },
                            onClick = {
                                val mediaId = entry.media?.id ?: entry.mediaId
                                if (mediaId != null) {
                                    viewModel.navigateTo(ScreenDestination.Detail(mediaId))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                action = {
                    data.visuals.actionLabel?.let { label ->
                        TextButton(onClick = { data.performAction() }) {
                            Text(label, color = AnimeCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = DarkSurfaceElevated,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(data.visuals.message, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun NotConnectedState(
    onConnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AnimeViolet, AnimePink)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Connect with AniList",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Link your AniList account with 1-tap using OAuth Implicit Grant. View your watch & read lists in a clean AniList-style grid, track progress, and power custom recommendations boosted by your 8+ rated favorites.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AnimeViolet,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .testTag("connect_anilist_button")
        ) {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Connect AniList", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌟 Features Unlocked:",
                    color = AnimeCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Client-side OAuth with Bearer header security\n• AniList Grid View for Watching, Completed, & Planning\n• +10% Similarity boost for genres you rated 8+\n• \"Based on your list\" personalized recommendations shelf",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ConnectedUserHeader(
    viewer: AniListViewer,
    affinityGenres: Set<String>,
    onDisconnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val avatarUrl = viewer.avatar?.large ?: viewer.avatar?.medium
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = viewer.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, AnimeViolet, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AnimeViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewer.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewer.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AnimeEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SYNCED", color = AnimeEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "AniList ID: ${viewer.id}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF5252)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("disconnect_anilist_button")
                ) {
                    Icon(imageVector = Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (affinityGenres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AnimePink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Top 8+ Rated Genres:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(affinityGenres.toList().take(5)) { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(genre.replaceFirstChar { it.uppercase() }, color = AnimeCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * AniList-style Grid Item:
 * - Cover image as the main box (portrait 2:3 aspect ratio, rounded corners)
 * - Badges overlaid cleanly on corners (status on top-left, user score on top-right, progress on bottom-right)
 * - Media title displayed cleanly below the image
 * - Status & progress details below the title
 */
@Composable
private fun UserMediaGridCard(
    entry: MediaListEntry,
    onIncrementProgress: (delta: Int, total: Int?) -> Unit,
    onClick: () -> Unit
) {
    val media = entry.media

    // Status colors and display text
    val statusColor = when (entry.status?.uppercase()) {
        "CURRENT" -> AnimeCyan
        "COMPLETED" -> AnimeEmerald
        "PLANNING" -> AnimeViolet
        "PAUSED" -> Color(0xFFFFB74D)
        "DROPPED" -> Color(0xFFFF5252)
        "REPEATING" -> AnimePink
        else -> TextSecondary
    }

    val statusLabel = when (entry.status?.uppercase()) {
        "CURRENT" -> if (media?.type == "MANGA") "Reading" else "Watching"
        "COMPLETED" -> "Completed"
        "PLANNING" -> "Planning"
        "PAUSED" -> "Paused"
        "DROPPED" -> "Dropped"
        "REPEATING" -> if (media?.type == "MANGA") "Rereading" else "Rewatching"
        else -> entry.status ?: ""
    }

    // Progress text calculation
    val currentProgress = entry.progress ?: 0
    val total = if (media?.type == "ANIME") media.episodes else media?.chapters
    val unit = if (media?.type == "ANIME") "Ep" else "Ch"
    val progressText = if (total != null && total > 0) {
        "$currentProgress / $total"
    } else {
        "$currentProgress"
    }

    val score = entry.score ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("user_list_entry_${entry.id}")
    ) {
        // 1. Portrait Poster Image Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
        ) {
            val coverUrl = media?.coverImage?.bestUrl
            AsyncImage(
                model = coverUrl,
                contentDescription = media?.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient overlay for badge contrast
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )

            // Top-Left Status Badge Overlay
            if (statusLabel.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top-Right User Score Badge Overlay
            if (score > 0.0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(
                            0.5.dp,
                            if (score >= 8.0) Color(0xFFFFD54F) else AnimeCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (score >= 8.0) Color(0xFFFFD54F) else AnimeCyan,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = entry.displayScoreFormatted,
                            color = if (score >= 8.0) Color(0xFFFFD54F) else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom-Right Progress Stepper Overlay
            ProgressStepperControl(
                progressText = "$unit $progressText",
                canDecrement = currentProgress > 0,
                canIncrement = total == null || total <= 0 || currentProgress < total,
                onDecrement = { onIncrementProgress(-1, total) },
                onIncrement = { onIncrementProgress(1, total) },
                entryId = entry.id,
                isOverlay = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Title displayed below the image
        Text(
            text = media?.displayTitle ?: "Untitled",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Format & Progress sub-text below the title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = media?.displayFormat ?: "",
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1
            )

            val year = media?.seasonYear ?: media?.startDate?.year
            if (year != null) {
                Text(
                    text = year.toString(),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun UserMediaListCard(
    entry: MediaListEntry,
    onIncrementProgress: (delta: Int, total: Int?) -> Unit,
    onClick: () -> Unit
) {
    val media = entry.media
    val statusColor = when (entry.status?.uppercase()) {
        "CURRENT" -> AnimeCyan
        "COMPLETED" -> AnimeEmerald
        "PLANNING" -> AnimeViolet
        "PAUSED" -> Color(0xFFFFB74D)
        "DROPPED" -> Color(0xFFFF5252)
        "REPEATING" -> AnimePink
        else -> TextSecondary
    }

    val statusLabel = when (entry.status?.uppercase()) {
        "CURRENT" -> if (media?.type == "MANGA") "Reading" else "Watching"
        "COMPLETED" -> "Completed"
        "PLANNING" -> "Planning"
        "PAUSED" -> "Paused"
        "DROPPED" -> "Dropped"
        "REPEATING" -> if (media?.type == "MANGA") "Rereading" else "Rewatching"
        else -> entry.status ?: ""
    }

    val currentProgress = entry.progress ?: 0
    val total = if (media?.type == "ANIME") media.episodes else media?.chapters
    val unit = if (media?.type == "ANIME") "Ep" else "Ch"
    val progressText = if (total != null && total > 0) {
        "$unit $currentProgress / $total"
    } else {
        "$unit $currentProgress"
    }

    val score = entry.score ?: 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("user_list_entry_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val coverUrl = media?.coverImage?.bestUrl
            AsyncImage(
                model = coverUrl,
                contentDescription = media?.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 54.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media?.displayTitle ?: "Untitled",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (statusLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = media?.displayFormat ?: "",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    val year = media?.seasonYear ?: media?.startDate?.year
                    if (year != null) {
                        Text(
                            text = "• $year",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressStepperControl(
                        progressText = progressText,
                        canDecrement = currentProgress > 0,
                        canIncrement = total == null || total <= 0 || currentProgress < total,
                        onDecrement = { onIncrementProgress(-1, total) },
                        onIncrement = { onIncrementProgress(1, total) },
                        entryId = entry.id,
                        isOverlay = false
                    )

                    if (score > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (score >= 8.0) Color(0xFFFFD54F) else AnimeCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = entry.displayScoreFormatted,
                                color = if (score >= 8.0) Color(0xFFFFD54F) else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepperControl(
    progressText: String,
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    entryId: Int,
    isOverlay: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isOverlay) Color.Black.copy(alpha = 0.85f) else DarkSurfaceElevated)
            .border(0.5.dp, DarkBorderSubtle, RoundedCornerShape(6.dp))
            // Consume clicks inside the stepper container so tapping disabled buttons or gaps never navigates to Detail
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = canDecrement,
            modifier = Modifier
                .size(22.dp)
                .testTag("progress_minus_$entryId")
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease progress",
                tint = if (canDecrement) AnimeCyan else TextMuted.copy(alpha = 0.35f),
                modifier = Modifier.size(13.dp)
            )
        }

        Text(
            text = progressText,
            color = if (isOverlay) TextPrimary else TextSecondary,
            fontSize = if (isOverlay) 10.sp else 12.sp,
            fontWeight = if (isOverlay) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .testTag("progress_text_$entryId")
        )

        IconButton(
            onClick = onIncrement,
            enabled = canIncrement,
            modifier = Modifier
                .size(22.dp)
                .testTag("progress_plus_$entryId")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase progress",
                tint = if (canIncrement) AnimeCyan else TextMuted.copy(alpha = 0.35f),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

