package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AniListNetwork
import com.example.data.model.AniListViewer
import com.example.data.model.DiscoveryFilter
import com.example.data.model.MediaItem
import com.example.data.model.MediaListCollection
import com.example.data.model.MediaListEntry
import com.example.data.model.MediaTypeSelection
import com.example.data.model.ScoredRecommendation
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AniListRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    data object SearchRecs : ScreenDestination()
    data object Discovery : ScreenDestination()
    data class Detail(val mediaId: Int) : ScreenDestination()
    data object MyList : ScreenDestination()
    data object Settings : ScreenDestination()
}

enum class MyListSortOption(val displayName: String) {
    RECENTLY_UPDATED("Recently Updated"),
    SCORE_HIGH_TO_LOW("Score: High to Low"),
    SCORE_LOW_TO_HIGH("Score: Low to High"),
    TITLE_A_Z("Title: A-Z")
}

class AnimeRecsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AniListRepository()
    val preferences = UserPreferences(application)

    // Current Screen
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.SearchRecs)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Navigation back stack
    private val backStack = mutableListOf<ScreenDestination>()
    private val _canNavigateBack = MutableStateFlow(false)
    val canNavigateBack: StateFlow<Boolean> = _canNavigateBack.asStateFlow()

    // Cache of loaded Media details & recommendations keyed by mediaId
    private val detailCache = mutableMapOf<Int, Pair<MediaItem, List<ScoredRecommendation>>>()
    private var detailJob: Job? = null

    // Media Type Selection (Anime | Manga | Manhwa)
    private val _selectedMediaType = MutableStateFlow(MediaTypeSelection.ANIME)
    val selectedMediaType: StateFlow<MediaTypeSelection> = _selectedMediaType.asStateFlow()

    // Search Bar State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Trending/Popular feed
    private val _trendingMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val trendingMedia: StateFlow<List<MediaItem>> = _trendingMedia.asStateFlow()

    private val _isLoadingTrending = MutableStateFlow(false)
    val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending.asStateFlow()

    // Detail & Recommendations State
    private val _detailMedia = MutableStateFlow<MediaItem?>(null)
    val detailMedia: StateFlow<MediaItem?> = _detailMedia.asStateFlow()

    private val _detailRecommendations = MutableStateFlow<List<ScoredRecommendation>>(emptyList())
    val detailRecommendations: StateFlow<List<ScoredRecommendation>> = _detailRecommendations.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    // Random Discovery State
    private val _discoveryFilter = MutableStateFlow(DiscoveryFilter(mediaType = MediaTypeSelection.ANIME))
    val discoveryFilter: StateFlow<DiscoveryFilter> = _discoveryFilter.asStateFlow()

    private val _discoveredList = MutableStateFlow<List<MediaItem>>(emptyList())
    val discoveredList: StateFlow<List<MediaItem>> = _discoveredList.asStateFlow()

    private val _featuredRandomItem = MutableStateFlow<MediaItem?>(null)
    val featuredRandomItem: StateFlow<MediaItem?> = _featuredRandomItem.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveryError = MutableStateFlow<String?>(null)
    val discoveryError: StateFlow<String?> = _discoveryError.asStateFlow()

    // --- AniList OAuth & User Profile State ---
    val aniListClientId = "49531"
    val aniListAuthUrl = "https://anilist.co/api/v2/oauth/authorize?client_id=$aniListClientId&response_type=token"

    private val _showOAuthDialog = MutableStateFlow(false)
    val showOAuthDialog: StateFlow<Boolean> = _showOAuthDialog.asStateFlow()

    private val _viewer = MutableStateFlow<AniListViewer?>(null)
    val viewer: StateFlow<AniListViewer?> = _viewer.asStateFlow()

    private val _isLoadingViewer = MutableStateFlow(false)
    val isLoadingViewer: StateFlow<Boolean> = _isLoadingViewer.asStateFlow()

    private val _viewerError = MutableStateFlow<String?>(null)
    val viewerError: StateFlow<String?> = _viewerError.asStateFlow()

    // User Media List
    private val _userMediaListCollection = MutableStateFlow<MediaListCollection?>(null)
    val userMediaListCollection: StateFlow<MediaListCollection?> = _userMediaListCollection.asStateFlow()

    private val _userListEntries = MutableStateFlow<List<MediaListEntry>>(emptyList())
    val userListEntries: StateFlow<List<MediaListEntry>> = _userListEntries.asStateFlow()

    private val _selectedUserListStatus = MutableStateFlow("ALL")
    val selectedUserListStatus: StateFlow<String> = _selectedUserListStatus.asStateFlow()

    private val _selectedUserListSort = MutableStateFlow(MyListSortOption.RECENTLY_UPDATED)
    val selectedUserListSort: StateFlow<MyListSortOption> = _selectedUserListSort.asStateFlow()

    private val _isUserListViewGrid = MutableStateFlow(true)
    val isUserListViewGrid: StateFlow<Boolean> = _isUserListViewGrid.asStateFlow()

    // Scroll position states per screen
    private val _searchScrollIndex = MutableStateFlow(0)
    private val _searchScrollOffset = MutableStateFlow(0)
    val searchScrollIndex: StateFlow<Int> = _searchScrollIndex.asStateFlow()
    val searchScrollOffset: StateFlow<Int> = _searchScrollOffset.asStateFlow()

    private val _myListScrollIndex = MutableStateFlow(0)
    private val _myListScrollOffset = MutableStateFlow(0)
    val myListScrollIndex: StateFlow<Int> = _myListScrollIndex.asStateFlow()
    val myListScrollOffset: StateFlow<Int> = _myListScrollOffset.asStateFlow()

    // Scroll position per Detail mediaId
    private val detailScrollPositions = mutableMapOf<Int, Pair<Int, Int>>()

    fun saveSearchScrollPosition(index: Int, offset: Int) {
        _searchScrollIndex.value = index
        _searchScrollOffset.value = offset
    }

    fun saveMyListScrollPosition(index: Int, offset: Int) {
        _myListScrollIndex.value = index
        _myListScrollOffset.value = offset
    }

    fun getDetailScrollPosition(mediaId: Int): Pair<Int, Int> {
        return detailScrollPositions[mediaId] ?: (0 to 0)
    }

    fun saveDetailScrollPosition(mediaId: Int, index: Int, offset: Int) {
        detailScrollPositions[mediaId] = index to offset
    }

    fun getCachedDetail(mediaId: Int): Pair<MediaItem, List<ScoredRecommendation>>? {
        return detailCache[mediaId]
    }

    private val _isLoadingUserList = MutableStateFlow(false)
    val isLoadingUserList: StateFlow<Boolean> = _isLoadingUserList.asStateFlow()

    private val _userListError = MutableStateFlow<String?>(null)
    val userListError: StateFlow<String?> = _userListError.asStateFlow()

    // User Affinity (genres/tags rated 8+ in existing list)
    private val _userAffinityGenres = MutableStateFlow<Set<String>>(emptySet())
    val userAffinityGenres: StateFlow<Set<String>> = _userAffinityGenres.asStateFlow()

    private val _userAffinityTags = MutableStateFlow<Set<String>>(emptySet())
    val userAffinityTags: StateFlow<Set<String>> = _userAffinityTags.asStateFlow()

    // "Based on your list" recommendations for homepage
    private val _personalizedRecommendations = MutableStateFlow<List<MediaItem>>(emptyList())
    val personalizedRecommendations: StateFlow<List<MediaItem>> = _personalizedRecommendations.asStateFlow()

    private val _isLoadingPersonalized = MutableStateFlow(false)
    val isLoadingPersonalized: StateFlow<Boolean> = _isLoadingPersonalized.asStateFlow()

    // Dialogs
    private val _showAgeConfirmDialog = MutableStateFlow(false)
    val showAgeConfirmDialog: StateFlow<Boolean> = _showAgeConfirmDialog.asStateFlow()

    // Search Debounce Job
    private var searchJob: Job? = null

    init {
        // Setup Token & Auth expiration handler from active account
        val activeAccount = preferences.getActiveAccount()
        if (activeAccount != null && activeAccount.token.isNotBlank()) {
            AniListNetwork.currentToken = activeAccount.token
            if (activeAccount.viewerId != 0) {
                _viewer.value = AniListViewer(
                    id = activeAccount.viewerId,
                    name = activeAccount.viewerName,
                    avatar = activeAccount.avatarUrl?.let { com.example.data.model.UserAvatar(large = it, medium = it) }
                )
            }
        }

        AniListNetwork.onAuthExpired = {
            viewModelScope.launch {
                val current = preferences.getActiveAccount()
                if (current != null) {
                    removeAccount(current.viewerId)
                } else {
                    disconnectAllAccounts()
                }
            }
        }

        loadTrendingMedia()
        performDiscovery()

        if (AniListNetwork.currentToken != null) {
            loadViewerAndList()
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value == destination) return

        if (backStack.lastOrNull() != _currentScreen.value) {
            backStack.add(_currentScreen.value)
        }
        _canNavigateBack.value = backStack.isNotEmpty()
        _currentScreen.value = destination

        if (destination is ScreenDestination.Detail) {
            loadMediaDetail(destination.mediaId)
        } else if (destination is ScreenDestination.MyList && _viewer.value != null && _userListEntries.value.isEmpty()) {
            loadUserMediaList()
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            _canNavigateBack.value = backStack.isNotEmpty()
            _currentScreen.value = previous
            if (previous is ScreenDestination.Detail) {
                loadMediaDetail(previous.mediaId)
            }
            return true
        } else if (_currentScreen.value is ScreenDestination.Detail) {
            _currentScreen.value = ScreenDestination.SearchRecs
            return true
        }
        return false
    }

    fun setMediaType(type: MediaTypeSelection) {
        if (_selectedMediaType.value != type) {
            _selectedMediaType.value = type
            _discoveryFilter.value = _discoveryFilter.value.copy(mediaType = type)
            // Reload search or trending for the new media type
            if (_searchQuery.value.isNotBlank()) {
                triggerSearch(_searchQuery.value)
            } else {
                loadTrendingMedia()
            }
            performDiscovery()

            if (_viewer.value != null) {
                loadUserMediaList()
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            _searchError.value = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // 350ms debounce
            triggerSearch(newQuery)
        }
    }

    fun triggerSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            val allowAdult = preferences.isAdultContentEnabled.value
            val result = repository.searchMedia(
                query = query,
                mediaType = _selectedMediaType.value,
                allowAdult = allowAdult
            )
            result.onSuccess { list ->
                _searchResults.value = list
                _isSearching.value = false
            }.onFailure { error ->
                _searchError.value = error.message ?: "Failed to search titles"
                _isSearching.value = false
            }
        }
    }

    fun loadTrendingMedia() {
        viewModelScope.launch {
            _isLoadingTrending.value = true
            val allowAdult = preferences.isAdultContentEnabled.value
            val result = repository.getTrendingMedia(
                mediaType = _selectedMediaType.value,
                allowAdult = allowAdult
            )
            result.onSuccess { list ->
                _trendingMedia.value = list
                _isLoadingTrending.value = false
            }.onFailure {
                _isLoadingTrending.value = false
            }
        }
    }

    fun loadMediaDetail(mediaId: Int, forceRefresh: Boolean = false) {
        detailJob?.cancel()
        if (!forceRefresh) {
            detailCache[mediaId]?.let { (cachedMedia, cachedRecs) ->
                _detailMedia.value = cachedMedia
                _detailRecommendations.value = cachedRecs
                _isLoadingDetail.value = false
                _detailError.value = null
                return
            }
        }

        _isLoadingDetail.value = true
        _detailError.value = null

        detailJob = viewModelScope.launch {
            val allowAdult = preferences.isAdultContentEnabled.value
            val result = repository.getMediaDetail(
                id = mediaId,
                allowAdult = allowAdult,
                userAffinityGenres = _userAffinityGenres.value,
                userAffinityTags = _userAffinityTags.value
            )
            result.onSuccess { (media, recs) ->
                detailCache[mediaId] = media to recs
                _detailMedia.value = media
                _detailRecommendations.value = recs
                _isLoadingDetail.value = false
            }.onFailure { error ->
                _detailError.value = error.message ?: "Failed to load media details"
                _isLoadingDetail.value = false
            }
        }
    }

    fun updateDiscoveryFilter(
        genre: String? = _discoveryFilter.value.genre,
        format: String? = _discoveryFilter.value.format,
        year: Int? = _discoveryFilter.value.year,
        status: String? = _discoveryFilter.value.status,
        mediaType: MediaTypeSelection = _discoveryFilter.value.mediaType
    ) {
        _discoveryFilter.value = DiscoveryFilter(
            genre = genre,
            format = format,
            year = year,
            status = status,
            mediaType = mediaType
        )
        performDiscovery()
    }

    fun performDiscovery() {
        viewModelScope.launch {
            _isDiscovering.value = true
            _discoveryError.value = null
            val allowAdult = preferences.isAdultContentEnabled.value
            val result = repository.discoverRandomMedia(_discoveryFilter.value, allowAdult)
            result.onSuccess { list ->
                _discoveredList.value = list
                _featuredRandomItem.value = list.firstOrNull()
                _isDiscovering.value = false
            }.onFailure { error ->
                _discoveryError.value = error.message ?: "Discovery failed"
                _isDiscovering.value = false
            }
        }
    }

    fun shuffleDiscovery() {
        if (_discoveredList.value.isNotEmpty()) {
            val shuffled = _discoveredList.value.shuffled()
            _discoveredList.value = shuffled
            _featuredRandomItem.value = shuffled.firstOrNull()
        } else {
            performDiscovery()
        }
    }

    // Multi-account observables
    val accounts: StateFlow<List<com.example.data.model.AniListAccount>> = preferences.accounts
    val activeViewerId: StateFlow<Int?> = preferences.activeViewerId

    // --- AniList OAuth & List Management Methods ---

    fun openAniListAuth() {
        _showOAuthDialog.value = true
    }

    fun dismissAniListAuth() {
        _showOAuthDialog.value = false
    }

    fun handleOAuthRedirectUrl(urlOrToken: String): Boolean {
        val token = extractTokenFromUrl(urlOrToken)
        if (!token.isNullOrBlank()) {
            _showOAuthDialog.value = false
            connectWithToken(token)
            return true
        }
        return false
    }

    private fun extractTokenFromUrl(input: String): String? {
        val trimmed = input.trim()
        // If user directly pasted an access token (alphanumeric/JWT token)
        if (!trimmed.contains("http") && !trimmed.contains("#") && !trimmed.contains("=") && trimmed.length > 20) {
            return trimmed
        }
        // Match access_token in URL fragment (#access_token=...) or query (?access_token=...)
        val regex = Regex("""[#&?]access_token=([^&]+)""")
        val match = regex.find(trimmed)
        return match?.groupValues?.getOrNull(1)
    }

    private fun connectWithToken(token: String) {
        viewModelScope.launch {
            _isLoadingViewer.value = true
            _viewerError.value = null
            AniListNetwork.currentToken = token

            val result = repository.getViewer()
            result.onSuccess { viewerData ->
                val avatar = viewerData.avatar?.large ?: viewerData.avatar?.medium
                val account = com.example.data.model.AniListAccount(
                    token = token,
                    viewerId = viewerData.id,
                    viewerName = viewerData.name,
                    avatarUrl = avatar
                )
                // Add to array without overwriting other accounts (checks viewerId first)
                preferences.addOrUpdateAccount(account)
                _viewer.value = viewerData
                _isLoadingViewer.value = false
                loadUserMediaList()
            }.onFailure { error ->
                _isLoadingViewer.value = false
                _viewerError.value = error.message ?: "Failed to verify AniList account"
                // Restore previous active token if any
                val active = preferences.getActiveAccount()
                AniListNetwork.currentToken = active?.token
            }
        }
    }

    fun switchAccount(viewerId: Int) {
        val target = preferences.accounts.value.find { it.viewerId == viewerId } ?: return
        preferences.setActiveViewerId(viewerId)
        AniListNetwork.currentToken = target.token

        // Reset list states while fetching
        _userMediaListCollection.value = null
        _userListEntries.value = emptyList()
        _userAffinityGenres.value = emptySet()
        _userAffinityTags.value = emptySet()
        _personalizedRecommendations.value = emptyList()

        _viewer.value = AniListViewer(
            id = target.viewerId,
            name = target.viewerName,
            avatar = target.avatarUrl?.let { com.example.data.model.UserAvatar(large = it, medium = it) }
        )

        loadViewerAndList()
    }

    fun removeAccount(viewerId: Int) {
        val wasActive = preferences.getActiveAccount()?.viewerId == viewerId
        preferences.removeAccount(viewerId)

        if (wasActive) {
            val nextActive = preferences.getActiveAccount()
            if (nextActive != null) {
                switchAccount(nextActive.viewerId)
            } else {
                // No accounts left
                AniListNetwork.currentToken = null
                _viewer.value = null
                _userMediaListCollection.value = null
                _userListEntries.value = emptyList()
                _userAffinityGenres.value = emptySet()
                _userAffinityTags.value = emptySet()
                _personalizedRecommendations.value = emptyList()
                _viewerError.value = null
                _userListError.value = null
            }
        }
    }

    fun disconnectAniList() {
        val current = preferences.getActiveAccount()
        if (current != null) {
            removeAccount(current.viewerId)
        } else {
            disconnectAllAccounts()
        }
    }

    fun disconnectAllAccounts() {
        preferences.clearAllAccounts()
        AniListNetwork.currentToken = null
        _viewer.value = null
        _userMediaListCollection.value = null
        _userListEntries.value = emptyList()
        _userAffinityGenres.value = emptySet()
        _userAffinityTags.value = emptySet()
        _personalizedRecommendations.value = emptyList()
        _viewerError.value = null
        _userListError.value = null
    }

    fun loadViewerAndList() {
        if (AniListNetwork.currentToken.isNullOrBlank()) return
        viewModelScope.launch {
            _isLoadingViewer.value = true
            _viewerError.value = null

            val result = repository.getViewer()
            result.onSuccess { viewerData ->
                _viewer.value = viewerData
                // Update stored account data if needed
                val currentToken = AniListNetwork.currentToken
                if (!currentToken.isNullOrBlank()) {
                    val avatar = viewerData.avatar?.large ?: viewerData.avatar?.medium
                    preferences.addOrUpdateAccount(
                        com.example.data.model.AniListAccount(
                            token = currentToken,
                            viewerId = viewerData.id,
                            viewerName = viewerData.name,
                            avatarUrl = avatar
                        )
                    )
                }
                _isLoadingViewer.value = false
                loadUserMediaList()
            }.onFailure { error ->
                _viewerError.value = error.message ?: "Failed to verify AniList account"
                _isLoadingViewer.value = false
                if (error.message?.contains("401") == true || error.message?.contains("authenticate", ignoreCase = true) == true) {
                    val current = preferences.getActiveAccount()
                    if (current != null) {
                        removeAccount(current.viewerId)
                    }
                }
            }
        }
    }

    fun loadUserMediaList() {
        val currentViewer = _viewer.value ?: return
        viewModelScope.launch {
            _isLoadingUserList.value = true
            _userListError.value = null

            val result = repository.getUserMediaList(currentViewer.id, _selectedMediaType.value)
            result.onSuccess { collection ->
                _userMediaListCollection.value = collection
                val allEntries = collection.lists.flatMap { it.entries }
                    .distinctBy { it.mediaId ?: it.id }
                _userListEntries.value = allEntries
                _isLoadingUserList.value = false

                // Calculate User Affinity (genres/tags rated 8+ in existing list)
                calculateUserAffinity(allEntries)
                loadPersonalizedRecommendations()
            }.onFailure { error ->
                _userListError.value = error.message ?: "Failed to fetch media list"
                _isLoadingUserList.value = false
            }
        }
    }

    private fun calculateUserAffinity(entries: List<MediaListEntry>) {
        val highlyRatedEntries = entries.filter { entry ->
            val s = entry.score ?: 0.0
            s >= 8.0 || (s > 10.0 && s >= 80.0)
        }

        val topGenres = highlyRatedEntries
            .flatMap { it.media?.genres ?: emptyList() }
            .map { it.lowercase().trim() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key }
            .toSet()

        val topTags = highlyRatedEntries
            .flatMap { it.media?.tags ?: emptyList() }
            .filter { it.isMediaSpoiler != true }
            .map { it.name.lowercase().trim() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
            .toSet()

        _userAffinityGenres.value = topGenres
        _userAffinityTags.value = topTags
    }

    fun loadPersonalizedRecommendations() {
        viewModelScope.launch {
            _isLoadingPersonalized.value = true
            val favGenres = _userAffinityGenres.value.toList().take(3)
            val completedIds = _userListEntries.value.mapNotNull { it.mediaId ?: it.media?.id }.toSet()
            val allowAdult = preferences.isAdultContentEnabled.value

            val result = repository.getPersonalizedRecommendations(
                favoriteGenres = favGenres,
                mediaType = _selectedMediaType.value,
                allowAdult = allowAdult,
                excludedIds = completedIds
            )
            result.onSuccess { items ->
                _personalizedRecommendations.value = items
                _isLoadingPersonalized.value = false
            }.onFailure {
                _isLoadingPersonalized.value = false
            }
        }
    }

    fun setUserListStatusFilter(status: String) {
        _selectedUserListStatus.value = status
    }

    fun setUserListSort(sort: MyListSortOption) {
        _selectedUserListSort.value = sort
    }

    fun setUserListViewGrid(isGrid: Boolean) {
        _isUserListViewGrid.value = isGrid
    }

    fun toggleUserListViewMode() {
        _isUserListViewGrid.value = !_isUserListViewGrid.value
    }

    fun requestToggleAdultContent(targetState: Boolean) {
        if (targetState) {
            // Turning ON -> check if user has confirmed 18+ age before
            if (preferences.hasConfirmedAge18.value) {
                preferences.setAdultContentEnabled(true)
                refreshCurrentDataWithAdultFilter()
            } else {
                _showAgeConfirmDialog.value = true
            }
        } else {
            // Turning OFF -> immediately disable
            preferences.setAdultContentEnabled(false)
            refreshCurrentDataWithAdultFilter()
        }
    }

    fun confirmAgeAndEnableAdult() {
        preferences.confirmAge18()
        preferences.setAdultContentEnabled(true)
        _showAgeConfirmDialog.value = false
        refreshCurrentDataWithAdultFilter()
    }

    fun dismissAgeDialog() {
        _showAgeConfirmDialog.value = false
    }

    fun toggleSaveCurrentMedia(mediaId: Int) {
        preferences.toggleSaveMedia(mediaId)
    }

    fun revealAdultContent(mediaId: Int) {
        preferences.revealAdultMedia(mediaId)
    }

    private fun refreshCurrentDataWithAdultFilter() {
        detailCache.clear()
        if (_searchQuery.value.isNotBlank()) {
            triggerSearch(_searchQuery.value)
        } else {
            loadTrendingMedia()
        }
        val currentDestination = _currentScreen.value
        val currentMediaId = (currentDestination as? ScreenDestination.Detail)?.mediaId ?: _detailMedia.value?.id
        if (currentMediaId != null) {
            loadMediaDetail(currentMediaId, forceRefresh = true)
        }
        performDiscovery()
        if (_viewer.value != null) {
            loadPersonalizedRecommendations()
        }
    }
}

