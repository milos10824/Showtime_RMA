package rs.edu.raf.showtime.feature.catalog

import rs.edu.raf.showtime.domain.movie.MovieListItem

data class GenreFilter(
    val id: Int?,
    val name: String,
)

data class MovieCatalogState(
    val movies: List<MovieListItem> = emptyList(),
    val query: String = "",
    val genre: GenreFilter = GenreFilter(null, "Svi"),
    val minYear: String = "",
    val maxYear: String = "",
    val minRating: String = "",
    val sortBy: String = "",
    val sortOrder: String = "desc",
    val page: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
)

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
    data class FavoriteChanged(val movieId: String, val value: Boolean) : MovieCatalogIntent
    data class WatchlistChanged(val movieId: String, val value: Boolean) : MovieCatalogIntent
}
