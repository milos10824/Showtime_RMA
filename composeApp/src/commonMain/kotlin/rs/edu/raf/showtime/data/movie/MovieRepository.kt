package rs.edu.raf.showtime.data.movie

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem

interface MovieRepository {
    fun observeMovies(): Flow<List<MovieListItem>>
    fun searchMovies(query: String): Flow<List<MovieListItem>>
    fun observeMovie(id: String): Flow<MovieDetails?>
    fun observeFavorites(): Flow<List<MovieListItem>>
    fun observeWatchlist(): Flow<List<MovieListItem>>
    fun observeFavoriteCount(): Flow<Int>
    fun observeWatchlistCount(): Flow<Int>

    suspend fun refreshMovies(
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Double? = null,
        sortBy: String = "",
        sortOrder: String = "desc",
    )

    suspend fun refreshMovieDetails(id: String)

    suspend fun syncFavorites()
    suspend fun syncWatchlist()

    suspend fun restoreCurrentUserMovieData()

    suspend fun getQuizPool(limit: Int): List<MovieDetails>

    suspend fun setFavorite(movieId: String, isFavorite: Boolean)
    suspend fun setWatchlisted(movieId: String, isWatchlisted: Boolean)

    suspend fun clearUserMovieData()
}
