package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.feature.quiz.QuizRules

class QuizRulesTest {

    @Test
    fun scoreUsesSpecifiedFormulaAndCapsAtOneHundred() {
        assertEquals(95.0, QuizRules.calculateScore(correctCount = 10, timeLeft = 30))
        assertEquals(100.0, QuizRules.calculateScore(correctCount = 11, timeLeft = 60))
    }

    @Test
    fun movieImageNeverRepeatsUsedImage() {
        val movie = movie(
            id = "target",
            poster = "poster.jpg",
            backdrop = "backdrop.jpg",
        )

        assertEquals(
            "poster.jpg",
            QuizRules.selectMovieImage(movie, usedImages = setOf("backdrop.jpg")),
        )
        assertNull(
            QuizRules.selectMovieImage(movie, usedImages = setOf("poster.jpg", "backdrop.jpg")),
        )
    }

    @Test
    fun wrongActorsExcludeEveryoneFromTargetMovie() {
        val target = movie(
            id = "target",
            cast = listOf("Lead", "Second", "Third", "Fourth"),
        )
        val others = listOf(
            target,
            movie("one", cast = listOf("Fourth", "Other A", "Other B")),
            movie("two", cast = listOf("Other C", "Other D")),
        )

        val wrongActors = QuizRules.selectWrongActors(target, others)

        assertEquals(3, wrongActors.size)
        assertTrue(wrongActors.distinct().size == wrongActors.size)
        assertFalse(wrongActors.any { it in target.castNames })
    }

    private fun movie(
        id: String,
        poster: String? = "poster-$id.jpg",
        backdrop: String? = "backdrop-$id.jpg",
        cast: List<String> = emptyList(),
    ): MovieDetails {
        return MovieDetails(
            imdbId = id,
            title = id,
            year = 2000,
            runtime = 120,
            overview = null,
            posterPath = poster,
            backdropPath = backdrop,
            genres = emptyList(),
            castNames = cast,
            imdbRating = null,
            imdbVotes = null,
            tmdbRating = null,
            isFavorite = false,
            isWatchlisted = false,
        )
    }
}
