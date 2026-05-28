package rs.edu.raf.showtime.data.movie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.showtime.data.movie.mapper.toDetails
import rs.edu.raf.showtime.data.movie.mapper.toEntity
import rs.edu.raf.showtime.data.movie.mapper.toListItem
import rs.edu.raf.showtime.database.dao.MovieDao
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.networking.ShowtimeApi

class DefaultMovieRepository(
    private val api: ShowtimeApi,
    private val movieDao: MovieDao,
) : MovieRepository {

    override fun observeMovies(): Flow<List<MovieListItem>> {
        return movieDao.observeMovies().map { movies -> movies.map { it.toListItem() } }
    }

    override fun searchMovies(query: String): Flow<List<MovieListItem>> {
        if (query.isBlank()) return observeMovies()
        return movieDao.searchMovies(query).map { movies -> movies.map { it.toListItem() } }
    }

    override fun observeMovie(id: String): Flow<MovieDetails?> {
        return movieDao.observeMovie(id).map { movie -> movie?.toDetails() }
    }

    override fun observeFavorites(): Flow<List<MovieListItem>> {
        return movieDao.observeFavorites().map { movies -> movies.map { it.toListItem() } }
    }

    override fun observeWatchlist(): Flow<List<MovieListItem>> {
        return movieDao.observeWatchlist().map { movies -> movies.map { it.toListItem() } }
    }

    override fun observeFavoriteCount(): Flow<Int> = movieDao.observeFavoriteCount()

    override fun observeWatchlistCount(): Flow<Int> = movieDao.observeWatchlistCount()

    override suspend fun refreshMovies(
        page: Int,
        pageSize: Int,
        query: String?,
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
    ) {
        val response = api.getMovies(
            page = page,
            pageSize = pageSize,
            query = query,
            genreId = genreId,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
        )

        val entities = response.items.map { item ->
            val oldMovie = movieDao.getMovie(item.imdbId)
            val newMovie = item.toEntity()
            newMovie.copy(
                isFavorite = oldMovie?.isFavorite ?: false,
                isWatchlisted = oldMovie?.isWatchlisted ?: false,
            )
        }

        movieDao.upsertMovies(entities)
    }

    override suspend fun refreshMovieDetails(id: String) {
        val previous = movieDao.getMovie(id)
        val movie = api.getMovie(id).toEntity(previous)
        movieDao.upsertMovie(movie)

        val castResponse = api.getMovieCast(id)
        val cast = castResponse.items.map { it.toEntity(movieId = id) }
        movieDao.upsertCast(cast)

        val castNames = cast.take(6).joinToString(",") { it.name }
        movieDao.upsertMovie(movie.copy(castNames = castNames))
    }

    override suspend fun setFavorite(movieId: String, isFavorite: Boolean) {
        movieDao.updateFavorite(movieId, isFavorite)
    }

    override suspend fun setWatchlisted(movieId: String, isWatchlisted: Boolean) {
        movieDao.updateWatchlist(movieId, isWatchlisted)
    }

    override suspend fun clearUserMovieData() {
        movieDao.clearUserMovieMarks()
    }
}
