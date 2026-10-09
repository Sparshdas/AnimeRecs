package com.example.domain

import com.example.data.model.MediaItem
import com.example.data.model.RecommendationNode
import com.example.data.model.RecommendationScore
import com.example.data.model.ScoredRecommendation
import kotlin.math.roundToInt

object RecommendationEngine {

    /**
     * Calculates recommendation score for a candidate media relative to the source target media.
     * Formula:
     * finalScore = (genreOverlap * 0.35) + (tagOverlap * 0.30) + (aniListRecScore * 0.20) + (formatMatch * 0.15) + userAffinityBoost
     *
     * If user has rated genres/tags 8+ in their list, an affinity boost of up to 10% is awarded.
     */
    fun calculateScore(
        target: MediaItem,
        candidate: MediaItem,
        aniListRating: Int? = null,
        userAffinityGenres: Set<String> = emptySet(),
        userAffinityTags: Set<String> = emptySet()
    ): RecommendationScore {
        // 1. Genre overlap (Jaccard similarity)
        val targetGenres = target.genres.map { it.lowercase().trim() }.toSet()
        val candidateGenres = candidate.genres.map { it.lowercase().trim() }.toSet()
        val genreOverlap: Double = if (targetGenres.isEmpty() && candidateGenres.isEmpty()) {
            0.5
        } else {
            val union = targetGenres union candidateGenres
            val intersection = targetGenres intersect candidateGenres
            if (union.isEmpty()) 0.0 else (intersection.size.toDouble() / union.size.toDouble())
        }

        // 2. Tag overlap (Jaccard similarity on non-spoiler tags)
        val targetTags = target.tags
            .filter { it.isMediaSpoiler != true }
            .map { it.name.lowercase().trim() }
            .toSet()
        val candidateTags = candidate.tags
            .filter { it.isMediaSpoiler != true }
            .map { it.name.lowercase().trim() }
            .toSet()
        val tagOverlap: Double = if (targetTags.isEmpty() && candidateTags.isEmpty()) {
            0.4
        } else {
            val union = targetTags union candidateTags
            val intersection = targetTags intersect candidateTags
            if (union.isEmpty()) 0.0 else (intersection.size.toDouble() / union.size.toDouble())
        }

        // 3. AniList Recommendation Community Score
        val aniListRecScore: Double = when {
            aniListRating != null && aniListRating > 0 -> {
                // Scaled from 0.45 to 1.0 based on community net upvotes
                (0.45 + (aniListRating.toDouble() / 20.0) * 0.55).coerceIn(0.45, 1.0)
            }
            aniListRating != null && aniListRating == 0 -> 0.35
            aniListRating != null && aniListRating < 0 -> 0.10
            else -> 0.30 // Fallback algorithmic match
        }

        // 4. Format match
        val formatMatch: Double = when {
            target.format != null && candidate.format != null && target.format.equals(candidate.format, ignoreCase = true) -> 1.0
            target.type != null && candidate.type != null && target.type.equals(candidate.type, ignoreCase = true) -> 0.70
            else -> 0.30
        }

        // 5. User List Affinity Boost (for genres/tags user rated 8+ in their list)
        val userGenreMatches = if (userAffinityGenres.isNotEmpty()) candidateGenres.intersect(userAffinityGenres).size else 0
        val userTagMatches = if (userAffinityTags.isNotEmpty()) candidateTags.intersect(userAffinityTags).size else 0
        val userAffinityBoost: Double = if (userGenreMatches > 0 || userTagMatches > 0) {
            ((userGenreMatches * 0.04) + (userTagMatches * 0.02)).coerceAtMost(0.10)
        } else {
            0.0
        }

        val baseScore = (genreOverlap * 0.35) + (tagOverlap * 0.30) + (aniListRecScore * 0.20) + (formatMatch * 0.15)
        val rawScore = baseScore + userAffinityBoost
        val percentage = (rawScore * 100.0).coerceIn(15.0, 99.0).roundToInt()

        return RecommendationScore(
            finalScorePercentage = percentage,
            genreOverlap = genreOverlap,
            tagOverlap = tagOverlap,
            aniListRecScore = aniListRecScore,
            formatMatch = formatMatch,
            userAffinityBoost = userAffinityBoost
        )
    }

    /**
     * Processes recommendation nodes from AniList query and blends with score calculation.
     */
    fun rankRecommendations(
        target: MediaItem,
        recNodes: List<RecommendationNode>,
        allowAdult: Boolean,
        userAffinityGenres: Set<String> = emptySet(),
        userAffinityTags: Set<String> = emptySet()
    ): List<ScoredRecommendation> {
        val seenIds = mutableSetOf<Int>()
        seenIds.add(target.id)

        val results = mutableListOf<ScoredRecommendation>()

        for (node in recNodes) {
            val candidate = node.mediaRecommendation ?: continue
            if (seenIds.contains(candidate.id)) continue
            if (!allowAdult && candidate.isAdult == true) continue

            seenIds.add(candidate.id)
            val score = calculateScore(target, candidate, node.rating, userAffinityGenres, userAffinityTags)
            results.add(ScoredRecommendation(candidate, score))
        }

        // Sort descending by final similarity percentage
        return results.sortedByDescending { it.score.finalScorePercentage }
    }
}
