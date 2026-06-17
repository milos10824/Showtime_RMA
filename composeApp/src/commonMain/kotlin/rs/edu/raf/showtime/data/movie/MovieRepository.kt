package rs.edu.raf.showtime.data.movie

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem

data class MovieCatalogQuery(
    val page: Int = 1,
    val pageSize: Int = 20,
    val query: String? = null,
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Double? = null,
    val sortBy: String = "imdb_votes",
    val sortOrder: String = "desc",
) {
    val cacheKey: String
        get() = listOf(
            query?.trim()?.lowercase().orEmpty(),
            genreId?.toString().orEmpty(),
            minYear?.toString().orEmpty(),
            maxYear?.toString().orEmpty(),
            minRating?.toString().orEmpty(),
            sortBy,
            sortOrder,
            pageSize.toString(),
        ).joinToString(separator = "|")
}

data class MovieCatalogPage(
    val movies: List<MovieListItem> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
)

data class MovieGenre(
    val id: Int,
    val name: String,
)

interface MovieRepository {
    fun observeCatalog(query: MovieCatalogQuery): Flow<MovieCatalogPage>
    fun observeGenres(): Flow<List<MovieGenre>>
    fun observeMovie(id: String): Flow<MovieDetails?>
    fun observeFavorites(): Flow<List<MovieListItem>>
    fun observeWatchlist(): Flow<List<MovieListItem>>
    fun observeFavoriteCount(): Flow<Int>
    fun observeWatchlistCount(): Flow<Int>

    suspend fun refreshCatalog(query: MovieCatalogQuery)
    suspend fun refreshGenres()
    suspend fun refreshMovieDetails(id: String)
    suspend fun syncFavorites()
    suspend fun syncWatchlist()
    suspend fun getQuizPool(limit: Int): List<MovieDetails>
    suspend fun setFavorite(movieId: String, isFavorite: Boolean)
    suspend fun setWatchlisted(movieId: String, isWatchlisted: Boolean)
}
