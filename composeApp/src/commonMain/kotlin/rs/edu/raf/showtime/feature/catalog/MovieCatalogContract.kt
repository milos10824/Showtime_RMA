package rs.edu.raf.showtime.feature.catalog

import rs.edu.raf.showtime.domain.movie.MovieListItem

data class GenreFilter(
    val id: Int?,
    val name: String,
)

data class MovieCatalogState(
    val movies: List<MovieListItem> = emptyList(),
    val genres: List<GenreFilter> = listOf(GenreFilter(null, "Svi")),
    val query: String = "",
    val genre: GenreFilter = GenreFilter(null, "Svi"),
    val minYear: String = "",
    val maxYear: String = "",
    val minRating: String = "",
    val sortBy: String = "imdb_votes",
    val sortOrder: String = "desc",
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val genreError: String? = null,
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && movies.isEmpty()

    val canGoNext: Boolean
        get() = totalPages > 0 && page < totalPages
}

sealed interface MovieCatalogIntent {
    data object Refresh : MovieCatalogIntent
    data object NextPage : MovieCatalogIntent
    data object PreviousPage : MovieCatalogIntent
    data class SearchChanged(val value: String) : MovieCatalogIntent
    data class GenreChanged(val value: GenreFilter) : MovieCatalogIntent
    data class MinYearChanged(val value: String) : MovieCatalogIntent
    data class MaxYearChanged(val value: String) : MovieCatalogIntent
    data class MinRatingChanged(val value: String) : MovieCatalogIntent
    data class SortChanged(val sortBy: String) : MovieCatalogIntent
    data class SortOrderChanged(val value: String) : MovieCatalogIntent
    data class FavoriteChanged(val movieId: String, val value: Boolean) : MovieCatalogIntent
    data class WatchlistChanged(val movieId: String, val value: Boolean) : MovieCatalogIntent
    data class MovieClicked(val movieId: String) : MovieCatalogIntent
    data object BackClicked : MovieCatalogIntent
}

sealed interface MovieCatalogEffect {
    data class OpenMovieDetails(val movieId: String) : MovieCatalogEffect
    data object Close : MovieCatalogEffect
}
