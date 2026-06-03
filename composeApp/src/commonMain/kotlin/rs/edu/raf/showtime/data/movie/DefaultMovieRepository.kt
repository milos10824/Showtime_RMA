package rs.edu.raf.showtime.data.movie

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.core.auth.UserMovieStore
import rs.edu.raf.showtime.data.movie.mapper.toDetails
import rs.edu.raf.showtime.data.movie.mapper.toEntity
import rs.edu.raf.showtime.data.movie.mapper.toListItem
import rs.edu.raf.showtime.database.dao.MovieDao
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.networking.ShowtimeApi
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel

class DefaultMovieRepository(
    private val api: ShowtimeApi,
    private val movieDao: MovieDao,
    private val authStore: AuthStore,
    private val userMovieStore: UserMovieStore,
) : MovieRepository {

    override fun observeMovies(): Flow<List<MovieListItem>> {
        return movieDao.observeMovies().map { movies ->
            movies.map { it.toListItem() }
        }
    }

    override fun searchMovies(query: String): Flow<List<MovieListItem>> {
        if (query.isBlank()) {
            return observeMovies()
        }

        return movieDao.searchMovies(query).map { movies ->
            movies.map { it.toListItem() }
        }
    }

    override fun observeMovie(id: String): Flow<MovieDetails?> {
        return movieDao.observeMovie(id).map { movie ->
            movie?.toDetails()
        }
    }

    override fun observeFavorites(): Flow<List<MovieListItem>> {
        return movieDao.observeFavorites().map { movies ->
            movies.map { it.toListItem() }
        }
    }

    override fun observeWatchlist(): Flow<List<MovieListItem>> {
        return movieDao.observeWatchlist().map { movies ->
            movies.map { it.toListItem() }
        }
    }

    override fun observeFavoriteCount(): Flow<Int> {
        return movieDao.observeFavoriteCount()
    }

    override fun observeWatchlistCount(): Flow<Int> {
        return movieDao.observeWatchlistCount()
    }

    override suspend fun refreshMovies(
        page: Int,
        pageSize: Int,
        query: String?,
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
        sortBy: String,
        sortOrder: String,
    ) {
        val response = api.getMovies(
            page = page,
            pageSize = pageSize,
            query = query,
            genreId = genreId,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
            sortBy = sortBy,
            sortOrder = sortOrder,
        )

        val username = currentUsername()
        val favoriteIds = userMovieStore.getFavorites(username)
        val watchlistIds = userMovieStore.getWatchlist(username)

        val entities = response.items.map { item ->
            val oldMovie = movieDao.getMovie(item.imdbId)
            val newMovie = item.toEntity()

            newMovie.copy(
                isFavorite = item.imdbId in favoriteIds || oldMovie?.isFavorite == true,
                isWatchlisted = item.imdbId in watchlistIds || oldMovie?.isWatchlisted == true,
            )
        }

        movieDao.upsertMovies(entities)
        restoreCurrentUserMovieData()
    }

    override suspend fun refreshMovieDetails(id: String) {
        val previous = movieDao.getMovie(id)
        val baseMovie = api.getMovie(id).toEntity(previous)
        movieDao.upsertMovie(baseMovie)

        var castNames = baseMovie.castNames
        var posterPath = baseMovie.posterPath
        var backdropPath = baseMovie.backdropPath

        try {
            val castResponse = api.getMovieCast(id, pageSize = 10)
            val cast = castResponse.items.map { it.toEntity(movieId = id) }
            movieDao.upsertCast(cast)
            castNames = cast.take(6).joinToString(",") { it.name }
        } catch (_: Exception) {
            // Detalji filma ostaju prikazani i ako cast endpoint trenutno ne uspe.
        }

        try {
            val images = api.getMovieImages(id)
            movieDao.upsertImages(images.posters.map { it.toEntity(id, "poster") })
            movieDao.upsertImages(images.backdrops.map { it.toEntity(id, "backdrop") })

            posterPath = images.posters.firstOrNull()?.filePath ?: posterPath
            backdropPath = images.backdrops.firstOrNull()?.filePath ?: backdropPath
        } catch (_: Exception) {
            // Ako nema posebnih slika, koristimo poster/backdrop iz osnovnog detail odgovora.
        }

        val username = currentUsername()
        val favoriteIds = userMovieStore.getFavorites(username)
        val watchlistIds = userMovieStore.getWatchlist(username)
        val currentMovie = movieDao.getMovie(id) ?: baseMovie

        movieDao.upsertMovie(
            currentMovie.copy(
                posterPath = posterPath,
                backdropPath = backdropPath,
                castNames = castNames,
                isFavorite = id in favoriteIds || currentMovie.isFavorite,
                isWatchlisted = id in watchlistIds || currentMovie.isWatchlisted,
            )
        )
    }

    override suspend fun syncFavorites() {
        val token = currentToken() ?: return
        val username = currentUsername()
        val localIds = userMovieStore.getFavorites(username)

        try {
            val favorites = api.getFavorites(token)
            val remoteIds = favorites.map { it.imdbId }.toSet()
            val mergedIds = localIds + remoteIds

            saveRemoteMovieMarks(
                movies = favorites,
                favorite = true,
            )

            userMovieStore.replaceFavorites(
                username = username,
                movieIds = mergedIds,
            )

            restoreCurrentUserMovieData()

            (localIds - remoteIds).forEach { movieId ->
                try {
                    api.addFavorite(token, movieId)
                } catch (_: Exception) {
                    // Ako server trenutno ne prihvati, lokalni podatak ostaje sacuvan.
                }
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
                movieDao.clearUserMovieMarks()
            }
        } catch (_: Exception) {
            restoreCurrentUserMovieData()
        }
    }

    override suspend fun syncWatchlist() {
        val token = currentToken() ?: return
        val username = currentUsername()
        val localIds = userMovieStore.getWatchlist(username)

        try {
            val watchlist = api.getWatchlist(token)
            val remoteIds = watchlist.map { it.imdbId }.toSet()
            val mergedIds = localIds + remoteIds

            saveRemoteMovieMarks(
                movies = watchlist,
                watchlist = true,
            )

            userMovieStore.replaceWatchlist(
                username = username,
                movieIds = mergedIds,
            )

            restoreCurrentUserMovieData()

            (localIds - remoteIds).forEach { movieId ->
                try {
                    api.addWatchlist(token, movieId)
                } catch (_: Exception) {
                    // Ako server trenutno ne prihvati, lokalni podatak ostaje sacuvan.
                }
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
                movieDao.clearUserMovieMarks()
            }
        } catch (_: Exception) {
            restoreCurrentUserMovieData()
        }
    }

    override suspend fun restoreCurrentUserMovieData() {
        val username = currentUsername()
        val favoriteIds = userMovieStore.getFavorites(username)
        val watchlistIds = userMovieStore.getWatchlist(username)

        movieDao.clearUserMovieMarks()

        favoriteIds.forEach { movieId ->
            movieDao.updateFavorite(movieId, true)
        }

        watchlistIds.forEach { movieId ->
            movieDao.updateWatchlist(movieId, true)
        }
    }

    override suspend fun getQuizPool(limit: Int): List<MovieDetails> {
        var pool = movieDao.getQuizPool(limit)

        if (pool.size < 10) {
            refreshMovies(
                page = 1,
                pageSize = 100,
                sortBy = "imdb_votes",
                sortOrder = "desc",
            )
            pool = movieDao.getQuizPool(limit)
        }

        pool.take(12).forEach { movie ->
            if (movie.castNames.isBlank() || movie.backdropPath.isNullOrBlank()) {
                try {
                    refreshMovieDetails(movie.imdbId)
                } catch (_: Exception) {
                    // Kviz može da nastavi i bez detalja za baš svaki film.
                }
            }
        }

        return movieDao.getQuizPool(limit).map { it.toDetails() }
    }

    override suspend fun setFavorite(movieId: String, isFavorite: Boolean) {
        val username = currentUsername()

        movieDao.updateFavorite(movieId, isFavorite)
        userMovieStore.setFavorite(
            username = username,
            movieId = movieId,
            isFavorite = isFavorite,
        )

        val token = currentToken() ?: return

        try {
            if (isFavorite) {
                api.addFavorite(token, movieId)
            } else {
                api.removeFavorite(token, movieId)
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
                movieDao.clearUserMovieMarks()
            }
        } catch (_: Exception) {
            // Ne vracamo na staro, jer korisnik ocekuje da lokalna izmena ostane zapamcena.
        }
    }

    override suspend fun setWatchlisted(movieId: String, isWatchlisted: Boolean) {
        val username = currentUsername()

        movieDao.updateWatchlist(movieId, isWatchlisted)
        userMovieStore.setWatchlisted(
            username = username,
            movieId = movieId,
            isWatchlisted = isWatchlisted,
        )

        val token = currentToken() ?: return

        try {
            if (isWatchlisted) {
                api.addWatchlist(token, movieId)
            } else {
                api.removeWatchlist(token, movieId)
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
                movieDao.clearUserMovieMarks()
            }
        } catch (_: Exception) {
            // Ne vracamo na staro, jer lokalna izmena treba da ostane vidljiva.
        }
    }

    override suspend fun clearUserMovieData() {
        movieDao.clearUserMovieMarks()
    }

    private suspend fun saveRemoteMovieMarks(
        movies: List<MovieListItemApiModel>,
        favorite: Boolean = false,
        watchlist: Boolean = false,
    ) {
        movies.forEach { item ->
            val oldMovie = movieDao.getMovie(item.imdbId)
            val entity = item.toEntity().copy(
                isFavorite = if (favorite) true else oldMovie?.isFavorite ?: false,
                isWatchlisted = if (watchlist) true else oldMovie?.isWatchlisted ?: false,
            )

            movieDao.upsertMovie(entity)
        }
    }

    private suspend fun currentToken(): String? {
        return authStore.authData.first().token
    }

    private suspend fun currentUsername(): String {
        return authStore.authData.first().username ?: "guest"
    }
}
