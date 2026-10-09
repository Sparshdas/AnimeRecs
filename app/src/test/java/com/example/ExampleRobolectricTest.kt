package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MediaItem
import com.example.data.model.MediaTag
import com.example.data.model.MediaTitle
import com.example.data.model.RecommendationScore
import com.example.domain.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AnimeRecs", appName)
  }

  @Test
  fun `recommendation engine calculates high similarity for matching genres and tags`() {
    val target = MediaItem(
      id = 1,
      title = MediaTitle(english = "Frieren: Beyond Journey's End"),
      format = "TV",
      type = "ANIME",
      genres = listOf("Adventure", "Drama", "Fantasy"),
      tags = listOf(MediaTag(name = "Magic"), MediaTag(name = "Elf"), MediaTag(name = "Coming of Age"))
    )

    val candidate = MediaItem(
      id = 2,
      title = MediaTitle(english = "Mushishi"),
      format = "TV",
      type = "ANIME",
      genres = listOf("Adventure", "Fantasy", "Supernatural"),
      tags = listOf(MediaTag(name = "Magic"), MediaTag(name = "Episodic"), MediaTag(name = "Philosophical"))
    )

    val score = RecommendationEngine.calculateScore(target, candidate, aniListRating = 15)
    assertTrue("Score should be positive", score.finalScorePercentage > 50)
    assertTrue("Genre overlap should be > 0", score.genreOverlap > 0.0)
    assertTrue("Tag overlap should be > 0", score.tagOverlap > 0.0)
    assertEquals(1.0, score.formatMatch, 0.01)

    // Test high match (90%+)
    val identicalCandidate = MediaItem(
      id = 3,
      title = MediaTitle(english = "Frieren Season 2"),
      format = "TV",
      type = "ANIME",
      genres = listOf("Adventure", "Drama", "Fantasy"),
      tags = listOf(MediaTag(name = "Magic"), MediaTag(name = "Elf"), MediaTag(name = "Coming of Age"))
    )
    val highMatchScore = RecommendationEngine.calculateScore(target, identicalCandidate, aniListRating = 20)
    assertTrue("Near identical candidate should score > 90%", highMatchScore.finalScorePercentage >= 90)
  }
}

