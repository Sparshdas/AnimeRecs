package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AniListAccount
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("animerecs_prefs", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val accountsListType = Types.newParameterizedType(List::class.java, AniListAccount::class.java)
    private val accountsAdapter = moshi.adapter<List<AniListAccount>>(accountsListType)

    // App-wide settings
    private val _isAdultContentEnabled = MutableStateFlow(prefs.getBoolean(KEY_IS_ADULT_ENABLED, false))
    val isAdultContentEnabled: StateFlow<Boolean> = _isAdultContentEnabled.asStateFlow()

    private val _hasConfirmedAge18 = MutableStateFlow(prefs.getBoolean(KEY_HAS_CONFIRMED_AGE_18, false))
    val hasConfirmedAge18: StateFlow<Boolean> = _hasConfirmedAge18.asStateFlow()

    private val _savedMediaIds = MutableStateFlow(loadSavedMediaIds())
    val savedMediaIds: StateFlow<Set<Int>> = _savedMediaIds.asStateFlow()

    private val _revealedAdultMediaIds = MutableStateFlow<Set<Int>>(emptySet())
    val revealedAdultMediaIds: StateFlow<Set<Int>> = _revealedAdultMediaIds.asStateFlow()

    // Multi-account management
    private val _accounts = MutableStateFlow<List<AniListAccount>>(loadAccounts())
    val accounts: StateFlow<List<AniListAccount>> = _accounts.asStateFlow()

    private val _activeViewerId = MutableStateFlow<Int?>(loadActiveViewerId())
    val activeViewerId: StateFlow<Int?> = _activeViewerId.asStateFlow()

    fun getActiveAccount(): AniListAccount? {
        val list = _accounts.value
        if (list.isEmpty()) return null
        val activeId = _activeViewerId.value ?: return list.first()
        return list.find { it.viewerId == activeId } ?: list.first()
    }

    fun addOrUpdateAccount(account: AniListAccount) {
        val current = _accounts.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.viewerId == account.viewerId }
        if (existingIndex >= 0) {
            current[existingIndex] = account
        } else {
            current.add(account)
        }
        saveAccounts(current)
        _accounts.value = current
        setActiveViewerId(account.viewerId)
    }

    fun removeAccount(viewerId: Int) {
        val current = _accounts.value.filter { it.viewerId != viewerId }
        saveAccounts(current)
        _accounts.value = current
        if (_activeViewerId.value == viewerId) {
            val nextActive = current.firstOrNull()?.viewerId
            setActiveViewerId(nextActive)
        }
    }

    fun clearAllAccounts() {
        saveAccounts(emptyList())
        _accounts.value = emptyList()
        setActiveViewerId(null)
    }

    fun setActiveViewerId(viewerId: Int?) {
        if (viewerId == null) {
            prefs.edit().remove(KEY_ACTIVE_VIEWER_ID).apply()
            _activeViewerId.value = null
        } else {
            prefs.edit().putInt(KEY_ACTIVE_VIEWER_ID, viewerId).apply()
            _activeViewerId.value = viewerId
        }
    }

    private fun loadAccounts(): List<AniListAccount> {
        val json = prefs.getString(KEY_ACCOUNTS_ARRAY, null)
        if (!json.isNullOrBlank()) {
            try {
                val parsed = accountsAdapter.fromJson(json)
                if (!parsed.isNullOrEmpty()) {
                    return parsed
                }
            } catch (_: Exception) {}
        }

        // Backward compatibility: check if single token was saved earlier
        val legacyToken = prefs.getString(KEY_LEGACY_ANILIST_TOKEN, null)
        if (!legacyToken.isNullOrBlank()) {
            return listOf(
                AniListAccount(
                    token = legacyToken,
                    viewerId = 0,
                    viewerName = "AniList User",
                    avatarUrl = null
                )
            )
        }
        return emptyList()
    }

    private fun saveAccounts(accounts: List<AniListAccount>) {
        try {
            val json = accountsAdapter.toJson(accounts)
            prefs.edit().putString(KEY_ACCOUNTS_ARRAY, json).apply()
        } catch (_: Exception) {}
    }

    private fun loadActiveViewerId(): Int? {
        if (!prefs.contains(KEY_ACTIVE_VIEWER_ID)) return null
        return prefs.getInt(KEY_ACTIVE_VIEWER_ID, 0).takeIf { it != 0 }
    }

    fun setAdultContentEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ADULT_ENABLED, enabled).apply()
        _isAdultContentEnabled.value = enabled
    }

    fun confirmAge18() {
        prefs.edit().putBoolean(KEY_HAS_CONFIRMED_AGE_18, true).apply()
        _hasConfirmedAge18.value = true
    }

    fun toggleSaveMedia(mediaId: Int): Boolean {
        val current = _savedMediaIds.value.toMutableSet()
        val isSavedNow = if (current.contains(mediaId)) {
            current.remove(mediaId)
            false
        } else {
            current.add(mediaId)
            true
        }
        saveMediaIds(current)
        _savedMediaIds.value = current
        return isSavedNow
    }

    fun isMediaSaved(mediaId: Int): Boolean {
        return _savedMediaIds.value.contains(mediaId)
    }

    fun revealAdultMedia(mediaId: Int) {
        _revealedAdultMediaIds.value = _revealedAdultMediaIds.value + mediaId
    }

    fun isAdultMediaRevealed(mediaId: Int): Boolean {
        return _revealedAdultMediaIds.value.contains(mediaId)
    }

    private fun loadSavedMediaIds(): Set<Int> {
        val raw = prefs.getStringSet(KEY_SAVED_MEDIA_IDS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveMediaIds(ids: Set<Int>) {
        val stringSet = ids.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_SAVED_MEDIA_IDS, stringSet).apply()
    }

    companion object {
        private const val KEY_IS_ADULT_ENABLED = "is_adult_enabled"
        private const val KEY_HAS_CONFIRMED_AGE_18 = "has_confirmed_age_18"
        private const val KEY_SAVED_MEDIA_IDS = "saved_media_ids"
        const val KEY_ACCOUNTS_ARRAY = "anilist_accounts"
        const val KEY_ACTIVE_VIEWER_ID = "active_anilist_viewer_id"
        const val KEY_LEGACY_ANILIST_TOKEN = "anilist_token"
    }
}
