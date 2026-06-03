package rs.edu.raf.showtime.feature.saved

import rs.edu.raf.showtime.domain.movie.MovieListItem

enum class SavedListType {
    FAVORITES,
    WATCHLIST,
}

data class SavedMoviesState(
    val type: SavedListType,
    val movies: List<MovieListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SavedMoviesIntent {
    data object Refresh : SavedMoviesIntent
    data class RemoveClicked(val movieId: String) : SavedMoviesIntent
}
