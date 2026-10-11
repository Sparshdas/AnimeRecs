package com.example.data.repository

import com.example.data.api.AniListApiService
import com.example.data.api.AniListNetwork
import com.example.data.model.DiscoveryFilter
import com.example.data.model.GraphQLRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaListEntry
import com.example.data.model.MediaTypeSelection
import com.example.data.model.ScoredRecommendation
import com.example.domain.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AniListRepository(
    private val api: AniListApiService = AniListNetwork.apiService
) {

    suspend fun searchMedia(
        query: String,
        mediaType: MediaTypeSelection,
        allowAdult: Boolean,
        page: Int = 1,
        perPage: Int = 20
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            val variables = mutableMapOf<String, Any?>(
                "type" to mediaType.toAniListType(),
                "page" to page,
                "perPage" to perPage
            )

            if (query.isNotBlank()) {
                variables["search"] = query
            }

            mediaType.toCountryCode()?.let {
                variables["countryOfOrigin"] = it
            }

            if (!allowAdult) {
                variables["isAdult"] = false
            }

            val request = GraphQLRequest(
                query = SEARCH_MEDIA_QUERY,
                variables = variables
            )

            val response = api.queryPage(request)
            val items = response.data?.page?.media ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingMedia(
        mediaType: MediaTypeSelection,
        allowAdult: Boolean,
        perPage: Int = 16
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            val variables = mutableMapOf<String, Any?>(
                "type" to mediaType.toAniListType(),
                "perPage" to perPage
            )

            mediaType.toCountryCode()?.let {
                variables["countryOfOrigin"] = it
            }

            if (!allowAdult) {
                variables["isAdult"] = false
            }

            val request = GraphQLRequest(
                query = TRENDING_MEDIA_QUERY,
                variables = variables
            )

            val response = api.queryPage(request)
            val items = response.data?.page?.media ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaDetail(
        id: Int,
        allowAdult: Boolean,
        userAffinityGenres: Set<String> = emptySet(),
        userAffinityTags: Set<String> = emptySet()
    ): Result<Pair<MediaItem, List<ScoredRecommendation>>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                query = MEDIA_DETAIL_QUERY,
                variables = mapOf("id" to id)
            )

            val response = api.queryMedia(request)
            val media = response.data?.media
                ?: return@withContext Result.failure(IllegalStateException("Media not found for id $id"))

            // Rank recommendations using the custom scoring formula with user affinity boost:
            // finalScore = (genreOverlap * 0.35) + (tagOverlap * 0.30) + (aniListRecScore * 0.20) + (formatMatch * 0.15) + userAffinityBoost
            val recNodes = media.recommendations?.nodes ?: emptyList()
            var scoredRecs = RecommendationEngine.rankRecommendations(
                media,
                recNodes,
                allowAdult,
                userAffinityGenres,
                userAffinityTags
            )

            // If AniList built-in recommendations are few (< 4), fetch similar media by genre/tags to enrich recommendations
            if (scoredRecs.size < 4 && media.genres.isNotEmpty()) {
                val fallbackItems = fetchSimilarByGenres(media, allowAdult)
                val existingIds = (scoredRecs.map { it.media.id } + media.id).toSet()
                val extraScored = fallbackItems
                    .filter { !existingIds.contains(it.id) }
                    .map { candidate ->
                        val score = RecommendationEngine.calculateScore(
                            media,
                            candidate,
                            null,
                            userAffinityGenres,
                            userAffinityTags
                        )
                        ScoredRecommendation(candidate, score)
                    }
                scoredRecs = (scoredRecs + extraScored).sortedByDescending { it.score.finalScorePercentage }
            }

            Result.success(Pair(media, scoredRecs))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchSimilarByGenres(
        target: MediaItem,
        allowAdult: Boolean
    ): List<MediaItem> {
        try {
            val genre = target.genres.firstOrNull() ?: return emptyList()
            val variables = mutableMapOf<String, Any?>(
                "genre" to genre,
                "type" to target.type,
                "page" to 1,
                "perPage" to 15
            )
            target.countryOfOrigin?.let { variables["countryOfOrigin"] = it }
            if (!allowAdult) variables["isAdult"] = false

            val request = GraphQLRequest(
                query = DISCOVERY_QUERY,
                variables = variables
            )
            val response = api.queryPage(request)
            return response.data?.page?.media ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun discoverRandomMedia(
        filter: DiscoveryFilter,
        allowAdult: Boolean
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            val randomPage = Random.nextInt(1, 6) // pick from top 5 pages to ensure rich popular results
            val variables = mutableMapOf<String, Any?>(
                "type" to filter.mediaType.toAniListType(),
                "page" to randomPage,
                "perPage" to 20
            )

            filter.mediaType.toCountryCode()?.let {
                variables["countryOfOrigin"] = it
            }

            if (!filter.genre.isNullOrBlank() && filter.genre != "All") {
                variables["genre"] = filter.genre
            }

            if (!filter.format.isNullOrBlank() && filter.format != "All") {
                variables["format"] = filter.format
            }

            if (filter.year != null && filter.year > 0) {
                variables["seasonYear"] = filter.year
            }

            if (!filter.status.isNullOrBlank() && filter.status != "All") {
                variables["status"] = filter.status
            }

            // Per prompt rules: Never show adult content in random discovery unless toggle is explicitly ON
            if (!allowAdult) {
                variables["isAdult"] = false
            }

            val request = GraphQLRequest(
                query = DISCOVERY_QUERY,
                variables = variables
            )

            val response = api.queryPage(request)
            val items = response.data?.page?.media ?: emptyList()

            // If empty (e.g. randomPage was too high for narrow filters), fallback to page 1
            if (items.isEmpty() && randomPage > 1) {
                variables["page"] = 1
                val fallbackResponse = api.queryPage(GraphQLRequest(DISCOVERY_QUERY, variables))
                val fallbackItems = fallbackResponse.data?.page?.media ?: emptyList()
                return@withContext Result.success(fallbackItems.shuffled())
            }

            Result.success(items.shuffled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getViewer(): Result<com.example.data.model.AniListViewer> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(query = VIEWER_QUERY)
            val response = api.queryViewer(request)
            val viewer = response.data?.viewer
                ?: return@withContext Result.failure(IllegalStateException("No viewer data returned. Check authentication."))
            Result.success(viewer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByName(username: String): Result<com.example.data.model.AniListViewer> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                query = USER_BY_NAME_QUERY,
                variables = mapOf("name" to username.trim())
            )
            val response = api.queryViewer(request)
            val user = response.data?.user
                ?: return@withContext Result.failure(IllegalStateException("AniList user '$username' not found."))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserMediaList(
        userId: Int,
        mediaType: MediaTypeSelection
    ): Result<com.example.data.model.MediaListCollection> = withContext(Dispatchers.IO) {
        try {
            val variables = mapOf(
                "userId" to userId,
                "type" to mediaType.toAniListType()
            )
            val request = GraphQLRequest(
                query = USER_MEDIA_LIST_QUERY,
                variables = variables
            )
            val response = api.queryMediaListCollection(request)
            val collection = response.data?.mediaListCollection
                ?: return@withContext Result.failure(IllegalStateException("Failed to load user list"))
            Result.success(collection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEntryProgress(entryId: Int, newProgress: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val mutation = """
                mutation (${'$'}id: Int, ${'$'}progress: Int) {
                    SaveMediaListEntry(id: ${'$'}id, progress: ${'$'}progress) {
                        id
                        progress
                    }
                }
            """.trimIndent()
            val variables = mapOf("id" to entryId, "progress" to newProgress)
            val response = api.updateProgress(GraphQLRequest(mutation, variables))
            val errorMsg = response.errors?.firstOrNull()?.message
            if (!errorMsg.isNullOrBlank()) {
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }
            val returnedProgress = response.data?.saveMediaListEntry?.progress
                ?: return@withContext Result.failure(IllegalStateException("Failed to update progress"))
            Result.success(returnedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEntryStatus(entryId: Int, status: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mutation = """
                mutation (${'$'}id: Int, ${'$'}status: MediaListStatus) {
                    SaveMediaListEntry(id: ${'$'}id, status: ${'$'}status) {
                        id
                        progress
                        status
                    }
                }
            """.trimIndent()
            val variables = mapOf("id" to entryId, "status" to status)
            val response = api.updateProgress(GraphQLRequest(mutation, variables))
            val errorMsg = response.errors?.firstOrNull()?.message
            if (!errorMsg.isNullOrBlank()) {
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }
            val returnedStatus = response.data?.saveMediaListEntry?.status ?: status
            Result.success(returnedStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateListEntry(mediaId: Int, status: String): Result<MediaListEntry> =
        withContext(Dispatchers.IO) {
            try {
                val mutation = """
                    mutation (${'$'}mediaId: Int, ${'$'}status: MediaListStatus) {
                        SaveMediaListEntry(mediaId: ${'$'}mediaId, status: ${'$'}status) {
                            id
                            mediaId
                            status
                            progress
                        }
                    }
                """.trimIndent()
                val variables = mapOf("mediaId" to mediaId, "status" to status)
                val response = api.updateProgress(GraphQLRequest(mutation, variables))
                val errorMsg = response.errors?.firstOrNull()?.message
                if (!errorMsg.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException(errorMsg))
                }
                val entry = response.data?.saveMediaListEntry
                    ?: return@withContext Result.failure(IllegalStateException("Failed to save entry"))
                Result.success(entry)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPersonalizedRecommendations(
        favoriteGenres: List<String>,
        mediaType: MediaTypeSelection,
        allowAdult: Boolean,
        excludedIds: Set<Int> = emptySet()
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            if (favoriteGenres.isEmpty()) {
                return@withContext getTrendingMedia(mediaType, allowAdult, perPage = 10)
            }
            val primaryGenre = favoriteGenres.firstOrNull() ?: "Action"
            val variables = mutableMapOf<String, Any?>(
                "genre" to primaryGenre,
                "type" to mediaType.toAniListType(),
                "page" to 1,
                "perPage" to 25
            )
            mediaType.toCountryCode()?.let { variables["countryOfOrigin"] = it }
            if (!allowAdult) variables["isAdult"] = false

            val request = GraphQLRequest(
                query = DISCOVERY_QUERY,
                variables = variables
            )
            val response = api.queryPage(request)
            val items = response.data?.page?.media ?: emptyList()
            val filtered = items.filter { !excludedIds.contains(it.id) }
            Result.success(filtered.take(12))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val VIEWER_QUERY = """
            query CurrentViewer {
              Viewer {
                id
                name
                about
                avatar {
                  large
                  medium
                }
                bannerImage
              }
            }
        """

        const val USER_BY_NAME_QUERY = """
            query UserByName(${"$"}name: String) {
              User(name: ${"$"}name) {
                id
                name
                about
                avatar {
                  large
                  medium
                }
                bannerImage
              }
            }
        """

        const val USER_MEDIA_LIST_QUERY = """
            query GetUserMediaList(${"$"}userId: Int, ${"$"}type: MediaType) {
              MediaListCollection(userId: ${"$"}userId, type: ${"$"}type) {
                lists {
                  name
                  isCustomList
                  status
                  entries {
                    id
                    mediaId
                    status
                    score(format: POINT_10_DECIMAL)
                    progress
                    progressVolumes
                    repeat
                    notes
                    updatedAt
                    media {
                      id
                      title {
                        romaji
                        english
                        native
                      }
                      type
                      format
                      status
                      episodes
                      chapters
                      genres
                      tags {
                        id
                        name
                        rank
                      }
                      countryOfOrigin
                      coverImage {
                        extraLarge
                        large
                        medium
                        color
                      }
                      averageScore
                      isAdult
                    }
                  }
                }
              }
            }
        """
        const val SEARCH_MEDIA_QUERY = """
            query SearchMedia(${"$"}search: String, ${"$"}type: MediaType, ${"$"}countryOfOrigin: CountryCode, ${"$"}isAdult: Boolean, ${"$"}page: Int, ${"$"}perPage: Int) {
              Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                pageInfo {
                  total
                  currentPage
                  lastPage
                  hasNextPage
                }
                media(search: ${"$"}search, type: ${"$"}type, countryOfOrigin: ${"$"}countryOfOrigin, isAdult: ${"$"}isAdult, sort: POPULARITY_DESC) {
                  id
                  title {
                    romaji
                    english
                    native
                  }
                  type
                  format
                  genres
                  tags {
                    id
                    name
                    rank
                    isMediaSpoiler
                  }
                  countryOfOrigin
                  description(asHtml: false)
                  coverImage {
                    extraLarge
                    large
                    medium
                    color
                  }
                  bannerImage
                  averageScore
                  meanScore
                  popularity
                  status
                  episodes
                  chapters
                  volumes
                  season
                  seasonYear
                  startDate {
                    year
                    month
                    day
                  }
                  studios(isMain: true) {
                    nodes {
                      id
                      name
                    }
                  }
                  isAdult
                }
              }
            }
        """

        const val TRENDING_MEDIA_QUERY = """
            query TrendingMedia(${"$"}type: MediaType, ${"$"}countryOfOrigin: CountryCode, ${"$"}isAdult: Boolean, ${"$"}perPage: Int) {
              Page(page: 1, perPage: ${"$"}perPage) {
                media(type: ${"$"}type, countryOfOrigin: ${"$"}countryOfOrigin, isAdult: ${"$"}isAdult, sort: [TRENDING_DESC, POPULARITY_DESC]) {
                  id
                  title {
                    romaji
                    english
                    native
                  }
                  type
                  format
                  genres
                  tags {
                    id
                    name
                    rank
                    isMediaSpoiler
                  }
                  countryOfOrigin
                  description(asHtml: false)
                  coverImage {
                    extraLarge
                    large
                    medium
                    color
                  }
                  bannerImage
                  averageScore
                  status
                  episodes
                  chapters
                  startDate {
                    year
                  }
                  studios(isMain: true) {
                    nodes {
                      id
                      name
                    }
                  }
                  isAdult
                }
              }
            }
        """

        const val MEDIA_DETAIL_QUERY = """
            query MediaDetail(${"$"}id: Int) {
              Media(id: ${"$"}id) {
                id
                title {
                  romaji
                  english
                  native
                }
                type
                format
                genres
                tags {
                  id
                  name
                  rank
                  isMediaSpoiler
                }
                countryOfOrigin
                description(asHtml: false)
                coverImage {
                  extraLarge
                  large
                  medium
                  color
                }
                bannerImage
                averageScore
                meanScore
                popularity
                status
                episodes
                chapters
                volumes
                season
                seasonYear
                startDate {
                  year
                  month
                  day
                }
                studios(isMain: true) {
                  nodes {
                    id
                    name
                  }
                }
                staff(perPage: 6) {
                  edges {
                    role
                    node {
                      id
                      name {
                        full
                      }
                    }
                  }
                }
                isAdult
                siteUrl
                recommendations(sort: RATING_DESC, perPage: 30) {
                  nodes {
                    rating
                    userRating
                    mediaRecommendation {
                      id
                      title {
                        romaji
                        english
                        native
                      }
                      type
                      format
                      genres
                      tags {
                        id
                        name
                        rank
                        isMediaSpoiler
                      }
                      countryOfOrigin
                      description(asHtml: false)
                      coverImage {
                        extraLarge
                        large
                        medium
                        color
                      }
                      bannerImage
                      averageScore
                      isAdult
                      episodes
                      chapters
                      status
                      startDate {
                        year
                      }
                    }
                  }
                }
              }
            }
        """

        const val DISCOVERY_QUERY = """
            query DiscoverMedia(${"$"}genre: String, ${"$"}format: MediaFormat, ${"$"}seasonYear: Int, ${"$"}status: MediaStatus, ${"$"}type: MediaType, ${"$"}countryOfOrigin: CountryCode, ${"$"}isAdult: Boolean, ${"$"}page: Int, ${"$"}perPage: Int) {
              Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                pageInfo {
                  total
                  currentPage
                  lastPage
                  hasNextPage
                }
                media(genre: ${"$"}genre, format: ${"$"}format, seasonYear: ${"$"}seasonYear, status: ${"$"}status, type: ${"$"}type, countryOfOrigin: ${"$"}countryOfOrigin, isAdult: ${"$"}isAdult, sort: [POPULARITY_DESC, SCORE_DESC]) {
                  id
                  title {
                    romaji
                    english
                    native
                  }
                  type
                  format
                  genres
                  tags {
                    id
                    name
                    rank
                    isMediaSpoiler
                  }
                  countryOfOrigin
                  description(asHtml: false)
                  coverImage {
                    extraLarge
                    large
                    medium
                    color
                  }
                  bannerImage
                  averageScore
                  meanScore
                  popularity
                  status
                  episodes
                  chapters
                  volumes
                  season
                  seasonYear
                  startDate {
                    year
                  }
                  studios(isMain: true) {
                    nodes {
                      id
                      name
                    }
                  }
                  isAdult
                }
              }
            }
        """
    }
}
