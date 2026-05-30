package rs.edu.raf.showtime.feature.catalog

import rs.edu.raf.showtime.domain.movie.MovieListItem

data class MovieCatalogState(
    val movies: List<MovieListItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface MovieCatalogIntent {
    data object Refresh : MovieCatalogIntent
    data class SearchChanged(val value: String) : MovieCatalogIntent
    data class FavoriteChanged(
        val movieId: String,
        val value: Boolean,
    ) : MovieCatalogIntent

    data class WatchlistChanged(
        val movieId: String,
        val value: Boolean,
    ) : MovieCatalogIntent
}
