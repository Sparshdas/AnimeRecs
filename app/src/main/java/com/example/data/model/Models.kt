package com.example.data.model

import com.squareup.moshi.Json

enum class MediaTypeSelection {
    ANIME,
    MANGA,
    MANHWA;

    fun toAniListType(): String {
        return when (this) {
            ANIME -> "ANIME"
            MANGA, MANHWA -> "MANGA"
        }
    }

    fun toCountryCode(): String? {
        return when (this) {
            MANHWA -> "KR"
            else -> null
        }
    }
}

data class GraphQLRequest(
    @Json(name = "query") val query: String,
    @Json(name = "variables") val variables: Map<String, Any?> = emptyMap()
)

data class AniListResponse<T>(
    @Json(name = "data") val data: T? = null,
    @Json(name = "errors") val errors: List<GraphQLError>? = null
)

data class GraphQLError(
    @Json(name = "message") val message: String? = null
)

data class PageContainer(
    @Json(name = "Page") val page: PageData? = null
)

data class MediaContainer(
    @Json(name = "Media") val media: MediaItem? = null
)

data class PageData(
    @Json(name = "pageInfo") val pageInfo: PageInfo? = null,
    @Json(name = "media") val media: List<MediaItem> = emptyList()
)

data class PageInfo(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "currentPage") val currentPage: Int? = null,
    @Json(name = "lastPage") val lastPage: Int? = null,
    @Json(name = "hasNextPage") val hasNextPage: Boolean? = null,
    @Json(name = "perPage") val perPage: Int? = null
)

data class MediaItem(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: MediaTitle? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "format") val format: String? = null,
    @Json(name = "genres") val genres: List<String> = emptyList(),
    @Json(name = "tags") val tags: List<MediaTag> = emptyList(),
    @Json(name = "countryOfOrigin") val countryOfOrigin: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "coverImage") val coverImage: MediaCoverImage? = null,
    @Json(name = "bannerImage") val bannerImage: String? = null,
    @Json(name = "averageScore") val averageScore: Int? = null,
    @Json(name = "meanScore") val meanScore: Int? = null,
    @Json(name = "popularity") val popularity: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "chapters") val chapters: Int? = null,
    @Json(name = "volumes") val volumes: Int? = null,
    @Json(name = "season") val season: String? = null,
    @Json(name = "seasonYear") val seasonYear: Int? = null,
    @Json(name = "startDate") val startDate: FuzzyDate? = null,
    @Json(name = "studios") val studios: StudioConnection? = null,
    @Json(name = "staff") val staff: StaffConnection? = null,
    @Json(name = "isAdult") val isAdult: Boolean? = false,
    @Json(name = "siteUrl") val siteUrl: String? = null,
    @Json(name = "recommendations") val recommendations: RecommendationConnection? = null
) {
    val displayTitle: String
        get() = title?.english ?: title?.romaji ?: title?.native ?: "Untitled"

    val cleanDescription: String
        get() {
            val raw = description ?: "No description available."
            return raw.replace(Regex("<[^>]*>"), "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#039;", "'")
        }

    val displayFormat: String
        get() {
            if (type == "MANGA" && countryOfOrigin == "KR") return "MANHWA"
            return format ?: type ?: "UNKNOWN"
        }

    val displayStudioOrAuthor: String
        get() {
            val mainStudio = studios?.nodes?.firstOrNull()?.name
            if (!mainStudio.isNullOrBlank()) return mainStudio

            val author = staff?.edges?.firstOrNull { edge ->
                val role = edge.role?.lowercase() ?: ""
                role.contains("story") || role.contains("art") || role.contains("original") || role.contains("creator")
            }?.node?.name?.full ?: staff?.edges?.firstOrNull()?.node?.name?.full

            return author ?: "Unknown Creator"
        }
}

data class MediaTitle(
    @Json(name = "romaji") val romaji: String? = null,
    @Json(name = "english") val english: String? = null,
    @Json(name = "native") val native: String? = null
)

data class MediaCoverImage(
    @Json(name = "extraLarge") val extraLarge: String? = null,
    @Json(name = "large") val large: String? = null,
    @Json(name = "medium") val medium: String? = null,
    @Json(name = "color") val color: String? = null
) {
    val bestUrl: String?
        get() = extraLarge ?: large ?: medium
}

data class MediaTag(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String,
    @Json(name = "rank") val rank: Int? = null,
    @Json(name = "isMediaSpoiler") val isMediaSpoiler: Boolean? = false
)

data class FuzzyDate(
    @Json(name = "year") val year: Int? = null,
    @Json(name = "month") val month: Int? = null,
    @Json(name = "day") val day: Int? = null
)

data class StudioConnection(
    @Json(name = "nodes") val nodes: List<StudioNode> = emptyList()
)

data class StudioNode(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null
)

data class StaffConnection(
    @Json(name = "edges") val edges: List<StaffEdge> = emptyList()
)

data class StaffEdge(
    @Json(name = "role") val role: String? = null,
    @Json(name = "node") val node: StaffNode? = null
)

data class StaffNode(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: StaffName? = null
)

data class StaffName(
    @Json(name = "full") val full: String? = null
)

data class RecommendationConnection(
    @Json(name = "nodes") val nodes: List<RecommendationNode> = emptyList()
)

data class RecommendationNode(
    @Json(name = "rating") val rating: Int? = null,
    @Json(name = "userRating") val userRating: String? = null,
    @Json(name = "mediaRecommendation") val mediaRecommendation: MediaItem? = null
)

data class RecommendationScore(
    val finalScorePercentage: Int, // 0..100
    val genreOverlap: Double,      // 0..1
    val tagOverlap: Double,        // 0..1
    val aniListRecScore: Double,   // 0..1
    val formatMatch: Double,       // 0..1
    val userAffinityBoost: Double = 0.0 // 0..0.10
) {
    enum class BadgeTier {
        HIGH,   // 90-100% -> Green
        MEDIUM, // 70-89% -> Yellow
        LOW     // < 70% -> Gray
    }

    val badgeTier: BadgeTier
        get() = when {
            finalScorePercentage >= 90 -> BadgeTier.HIGH
            finalScorePercentage >= 70 -> BadgeTier.MEDIUM
            else -> BadgeTier.LOW
        }
}

data class ScoredRecommendation(
    val media: MediaItem,
    val score: RecommendationScore
)

data class DiscoveryFilter(
    val genre: String? = null,
    val format: String? = null,
    val year: Int? = null,
    val status: String? = null,
    val mediaType: MediaTypeSelection = MediaTypeSelection.ANIME
)

data class AniListAccount(
    @Json(name = "token") val token: String,
    @Json(name = "viewerId") val viewerId: Int,
    @Json(name = "viewerName") val viewerName: String,
    @Json(name = "avatarUrl") val avatarUrl: String? = null
)

data class ViewerContainer(
    @Json(name = "Viewer") val viewer: AniListViewer? = null,
    @Json(name = "User") val user: AniListViewer? = null
)

data class AniListViewer(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "about") val about: String? = null,
    @Json(name = "avatar") val avatar: UserAvatar? = null,
    @Json(name = "bannerImage") val bannerImage: String? = null
)

data class UserAvatar(
    @Json(name = "large") val large: String? = null,
    @Json(name = "medium") val medium: String? = null
)

data class MediaListCollectionContainer(
    @Json(name = "MediaListCollection") val mediaListCollection: MediaListCollection? = null
)

data class MediaListCollection(
    @Json(name = "lists") val lists: List<MediaListGroup> = emptyList()
)

data class MediaListGroup(
    @Json(name = "name") val name: String? = null,
    @Json(name = "isCustomList") val isCustomList: Boolean? = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "entries") val entries: List<MediaListEntry> = emptyList()
)

data class MediaListEntry(
    @Json(name = "id") val id: Int,
    @Json(name = "mediaId") val mediaId: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "score") val score: Double? = 0.0,
    @Json(name = "progress") val progress: Int? = 0,
    @Json(name = "progressVolumes") val progressVolumes: Int? = 0,
    @Json(name = "repeat") val repeat: Int? = 0,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "updatedAt") val updatedAt: Long? = null,
    @Json(name = "media") val media: MediaItem? = null
) {
    val displayScoreFormatted: String
        get() {
            val s = score ?: 0.0
            return if (s <= 0.0) "-" else if (s % 1.0 == 0.0) "${s.toInt()}/10" else "$s/10"
        }
}

typealias UserMediaListEntry = MediaListEntry

data class SaveMediaListEntryContainer(
    @Json(name = "SaveMediaListEntry") val saveMediaListEntry: MediaListEntry? = null
)

typealias SavedMediaListEntry = MediaListEntry


