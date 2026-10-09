package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.data.model.RecommendationScore
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GrayMatch
import com.example.ui.theme.GrayMatchBg
import com.example.ui.theme.GreenMatch
import com.example.ui.theme.GreenMatchBg
import com.example.ui.theme.RedAdult
import com.example.ui.theme.RedAdultBg
import com.example.ui.theme.ScoreGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YellowMatch
import com.example.ui.theme.YellowMatchBg
import kotlin.math.roundToInt

@Composable
fun SimilarityBadge(
    score: RecommendationScore,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tier = score.badgeTier
    val badgeColor = when (tier) {
        RecommendationScore.BadgeTier.HIGH -> GreenMatch
        RecommendationScore.BadgeTier.MEDIUM -> YellowMatch
        RecommendationScore.BadgeTier.LOW -> GrayMatch
    }
    val badgeBg = when (tier) {
        RecommendationScore.BadgeTier.HIGH -> GreenMatchBg.copy(alpha = 0.9f)
        RecommendationScore.BadgeTier.MEDIUM -> YellowMatchBg.copy(alpha = 0.9f)
        RecommendationScore.BadgeTier.LOW -> GrayMatchBg.copy(alpha = 0.9f)
    }
    val icon = when (tier) {
        RecommendationScore.BadgeTier.HIGH -> Icons.Default.AutoAwesome
        RecommendationScore.BadgeTier.MEDIUM -> Icons.Default.CheckCircle
        RecommendationScore.BadgeTier.LOW -> Icons.Default.Info
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f)),
        modifier = modifier
            .testTag("similarity_badge_${score.finalScorePercentage}")
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${score.finalScorePercentage}% Match",
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdultBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        color = RedAdultBg.copy(alpha = 0.95f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RedAdult.copy(alpha = 0.8f)),
        modifier = modifier.testTag("adult_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = RedAdult,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "18+",
                color = RedAdult,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun MediaCard(
    media: MediaItem,
    similarityScore: RecommendationScore? = null,
    isAdultRevealed: Boolean = false,
    onRevealAdult: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBreakdownDialog by remember { mutableStateOf(false) }
    val isAdult = media.isAdult == true
    val shouldBlur = isAdult && !isAdultRevealed

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("media_card_${media.id}")
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Poster Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(DarkSurfaceElevated)
            ) {
                AsyncImage(
                    model = media.coverImage?.bestUrl,
                    contentDescription = media.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (shouldBlur) Modifier.blur(22.dp)
                            else Modifier
                        )
                )

                // Dark gradient overlay at bottom of poster
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 150f
                            )
                        )
                )

                // Similarity Badge at top-left
                if (similarityScore != null) {
                    SimilarityBadge(
                        score = similarityScore,
                        onClick = { showBreakdownDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }

                // 18+ Adult Badge at top-right
                if (isAdult) {
                    AdultBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                // If adult and blurred: Show Click to Reveal Overlay
                if (shouldBlur) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { onRevealAdult() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RedAdult,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "18+ Adult",
                                color = RedAdult,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Tap to view",
                                        color = Color.White,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Score pill at bottom left of image
                if (media.averageScore != null && media.averageScore > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ScoreGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${media.averageScore}%",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Format badge at bottom right of image
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = media.displayFormat,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Info text block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = media.displayTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val year = media.startDate?.year ?: media.seasonYear
                    Text(
                        text = if (year != null) "$year" else media.status?.replace("_", " ") ?: "",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    val episodesOrChapters = when {
                        media.episodes != null -> "${media.episodes} eps"
                        media.chapters != null -> "${media.chapters} chs"
                        else -> ""
                    }
                    if (episodesOrChapters.isNotEmpty()) {
                        Text(
                            text = episodesOrChapters,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showBreakdownDialog && similarityScore != null) {
        SimilarityBreakdownDialog(
            title = media.displayTitle,
            score = similarityScore,
            onDismiss = { showBreakdownDialog = false }
        )
    }
}

@Composable
fun SimilarityBreakdownDialog(
    title: String,
    score: RecommendationScore,
    onDismiss: () -> Unit
) {
    val tierColor = when (score.badgeTier) {
        RecommendationScore.BadgeTier.HIGH -> GreenMatch
        RecommendationScore.BadgeTier.MEDIUM -> YellowMatch
        RecommendationScore.BadgeTier.LOW -> GrayMatch
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Column {
                Text(
                    text = "Recommendation Similarity",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Final Score Banner
                Surface(
                    color = tierColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Weighted Match Score",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = when (score.badgeTier) {
                                    RecommendationScore.BadgeTier.HIGH -> "Exceptional Match (90-100%)"
                                    RecommendationScore.BadgeTier.MEDIUM -> "Strong Match (70-89%)"
                                    RecommendationScore.BadgeTier.LOW -> "Moderate Match (< 70%)"
                                },
                                color = tierColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${score.finalScorePercentage}%",
                            color = tierColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "Formula Weights Breakdown:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // 1. Genre Overlap (35%)
                ScoreBarItem(
                    label = "Genre Overlap (35%)",
                    value = score.genreOverlap,
                    weightText = "+${(score.genreOverlap * 35).roundToInt()}%"
                )

                // 2. Tag Overlap (30%)
                ScoreBarItem(
                    label = "Themes & Tags Overlap (30%)",
                    value = score.tagOverlap,
                    weightText = "+${(score.tagOverlap * 30).roundToInt()}%"
                )

                // 3. AniList Community Votes (20%)
                ScoreBarItem(
                    label = "AniList Community Signal (20%)",
                    value = score.aniListRecScore,
                    weightText = "+${(score.aniListRecScore * 20).roundToInt()}%"
                )

                // 4. Format & Type Match (15%)
                ScoreBarItem(
                    label = "Format & Type Alignment (15%)",
                    value = score.formatMatch,
                    weightText = "+${(score.formatMatch * 15).roundToInt()}%"
                )

                // 5. Personal List Affinity Boost (Up to 10%)
                if (score.userAffinityBoost > 0.0) {
                    ScoreBarItem(
                        label = "⭐ Your 8+ List Affinity Boost",
                        value = (score.userAffinityBoost / 0.10).coerceIn(0.0, 1.0),
                        weightText = "+${(score.userAffinityBoost * 100).roundToInt()}%"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun ScoreBarItem(
    label: String,
    value: Double,
    weightText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "$weightText (${(value * 100).roundToInt()}%)",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value.toFloat().coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.primary,
            trackColor = DarkBorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun AgeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        icon = {
            Surface(
                color = RedAdultBg,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = RedAdult,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Age Verification Required",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AniList includes adult and 18+ titles (Hentai / Mature / Ecchi).",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Surface(
                    color = DarkSurfaceCard,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "• All 18+ covers will remain blurred until clicked.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Adult titles will be tagged with a red 18+ badge.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• You can turn this off anytime in Settings.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = "Please self-attest: Are you 18 years of age or older?",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RedAdult),
                modifier = Modifier.testTag("confirm_age_18_button")
            ) {
                Text("I am 18 or older", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun MediaTypeToggleBar(
    selectedType: com.example.data.model.MediaTypeSelection,
    onTypeSelected: (com.example.data.model.MediaTypeSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val types = listOf(
            Triple(com.example.data.model.MediaTypeSelection.ANIME, "Anime", "🎌"),
            Triple(com.example.data.model.MediaTypeSelection.MANGA, "Manga", "📖"),
            Triple(com.example.data.model.MediaTypeSelection.MANHWA, "Manhwa", "🇰🇷")
        )

        types.forEach { (type, title, icon) ->
            val isSelected = selectedType == type
            val bg = if (isSelected) com.example.ui.theme.AnimeViolet else Color.Transparent
            val textCol = if (isSelected) Color.White else TextSecondary

            Surface(
                color = bg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(type) }
                    .testTag("tab_${type.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = textCol,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

