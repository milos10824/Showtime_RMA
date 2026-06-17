package rs.edu.raf.showtime.feature.quiz

import rs.edu.raf.showtime.domain.movie.MovieDetails
import kotlin.math.min

internal object QuizRules {
    fun calculateScore(correctCount: Int, timeLeft: Int): Double {
        return min(100.0, correctCount * (9.0 + timeLeft / 60.0))
    }

    fun selectMovieImage(movie: MovieDetails, usedImages: Set<String>): String? {
        return listOfNotNull(movie.backdropPath, movie.posterPath)
            .filter { it.isNotBlank() }
            .distinct()
            .filterNot { it in usedImages }
            .randomOrNull()
    }

    fun selectWrongActors(
        targetMovie: MovieDetails,
        movies: List<MovieDetails>,
    ): List<String> {
        val targetActors = targetMovie.castNames.toSet()
        return movies
            .asSequence()
            .filter { it.imdbId != targetMovie.imdbId }
            .flatMap { it.castNames.asSequence() }
            .filterNot { it in targetActors }
            .distinct()
            .shuffled()
            .take(3)
            .toList()
    }
}
