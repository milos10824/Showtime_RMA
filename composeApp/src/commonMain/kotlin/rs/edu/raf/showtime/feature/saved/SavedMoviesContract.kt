package rs.edu.raf.showtime.feature.saved

import rs.edu.raf.showtime.domain.movie.MovieListItem

enum class SavedListType {
    FAVORITE,
    WATCHLIST,
}

data class SavedMoviesState(
    val type: SavedListType,
    val movies: List<MovieListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && movies.isEmpty()
}

sealed interface SavedMoviesIntent {
    data object Refresh : SavedMoviesIntent
    data class RemoveClicked(val movieId: String) : SavedMoviesIntent
    data class MovieClicked(val movieId: String) : SavedMoviesIntent
    data object BackClicked : SavedMoviesIntent
}

sealed interface SavedMoviesEffect {
    data class OpenMovieDetails(val movieId: String) : SavedMoviesEffect
    data object Close : SavedMoviesEffect
}
