package rs.edu.raf.showtime.feature.details

import rs.edu.raf.showtime.domain.movie.MovieDetails

data class MovieDetailsState(
    val movie: MovieDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface MovieDetailsIntent {
    data object Refresh : MovieDetailsIntent
    data class FavoriteChanged(val movieId: String, val value: Boolean) : MovieDetailsIntent
    data class WatchlistChanged(val movieId: String, val value: Boolean) : MovieDetailsIntent
}
