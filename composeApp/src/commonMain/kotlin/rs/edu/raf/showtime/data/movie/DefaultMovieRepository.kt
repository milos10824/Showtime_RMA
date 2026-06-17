package rs.edu.raf.showtime.data.movie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.data.movie.mapper.isActor
import rs.edu.raf.showtime.data.movie.mapper.toDetails
import rs.edu.raf.showtime.data.movie.mapper.toEntity
import rs.edu.raf.showtime.data.movie.mapper.toListItem
import rs.edu.raf.showtime.database.dao.MovieDao
import rs.edu.raf.showtime.database.entity.CatalogEntryEntity
import rs.edu.raf.showtime.database.entity.CatalogPageEntity
import rs.edu.raf.showtime.database.entity.GenreEntity
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.networking.ShowtimeApi
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel

class DefaultMovieRepository(
    private val api: ShowtimeApi,
    private val movieDao: MovieDao,
    private val authStore: AuthStore,
) : MovieRepository {

    override fun observeCatalog(query: MovieCatalogQuery): Flow<MovieCatalogPage> {
        val safePage = query.page.coerceAtLeast(1)
        return combine(
            movieDao.observeCatalogMovies(query.cacheKey, safePage),
            movieDao.observeCatalogPage(query.cacheKey, safePage),
        ) { movies, page ->
            MovieCatalogPage(
                movies = movies.map { it.toListItem() },
                page = page?.page ?: safePage,
                pageSize = page?.pageSize ?: query.pageSize,
                totalItems = page?.totalItems ?: 0,
                totalPages = page?.totalPages ?: 0,
            )
        }
    }

    override fun observeGenres(): Flow<List<MovieGenre>> {
        return movieDao.observeGenres().map { genres ->
            genres.map { MovieGenre(id = it.id, name = it.name) }
        }
    }

    override fun observeMovie(id: String): Flow<MovieDetails?> {
        return combine(
            movieDao.observeMovie(id),
            movieDao.observeImagesForMovie(id),
            movieDao.observeCastForMovie(id),
        ) { movie, images, cast ->
            movie?.toDetails()?.copy(
                posterPath = images.firstOrNull { it.type == "poster" }?.filePath ?: movie.posterPath,
                backdropPath = images.firstOrNull { it.type == "backdrop" }?.filePath ?: movie.backdropPath,
                castNames = cast.map { it.name }.ifEmpty { movie.toDetails().castNames },
            )
        }
    }

    override fun observeFavorites(): Flow<List<MovieListItem>> {
        return movieDao.observeFavorites().map { movies -> movies.map { it.toListItem() } }
    }

    override fun observeWatchlist(): Flow<List<MovieListItem>> {
        return movieDao.observeWatchlist().map { movies -> movies.map { it.toListItem() } }
    }

    override fun observeFavoriteCount(): Flow<Int> = movieDao.observeFavoriteCount()

    override fun observeWatchlistCount(): Flow<Int> = movieDao.observeWatchlistCount()

    override suspend fun refreshCatalog(query: MovieCatalogQuery) {
        val response = api.getMovies(
            page = query.page,
            pageSize = query.pageSize,
            query = query.query,
            genreId = query.genreId,
            minYear = query.minYear,
            maxYear = query.maxYear,
            minRating = query.minRating,
            sortBy = query.sortBy,
            sortOrder = query.sortOrder,
        )

        val entities = response.items.map { item ->
            val oldMovie = movieDao.getMovie(item.imdbId)
            item.toEntity(previous = oldMovie)
        }
        val entries = response.items.mapIndexed { index, item ->
            CatalogEntryEntity(
                queryKey = query.cacheKey,
                page = response.page,
                movieId = item.imdbId,
                position = index,
            )
        }
        val page = CatalogPageEntity(
            queryKey = query.cacheKey,
            page = response.page,
            pageSize = response.pageSize,
            totalItems = response.totalItems,
            totalPages = response.totalPages,
        )

        movieDao.replaceCatalogPage(page = page, entries = entries, movies = entities)
    }

    override suspend fun refreshGenres() {
        val genres = api.getGenres().map { GenreEntity(id = it.id, name = it.name) }
        movieDao.upsertGenres(genres)
    }

    override suspend fun refreshMovieDetails(id: String) {
        val previous = movieDao.getMovie(id)
        val baseMovie = api.getMovie(id).toEntity(previous)
        movieDao.upsertMovie(baseMovie)

        var castNames = baseMovie.castNames
        var posterPath = baseMovie.posterPath
        var backdropPath = baseMovie.backdropPath
        var enrichmentError: Throwable? = null

        try {
            val firstPage = api.getMovieCast(id, page = 1, pageSize = 100)
            val castModels = firstPage.items.toMutableList()
            for (page in 2..firstPage.totalPages) {
                castModels += api.getMovieCast(id, page = page, pageSize = 100).items
            }
            val cast = castModels
                .filter { it.isActor() }
                .distinctBy { it.imdbId }
                .map { it.toEntity(movieId = id) }
            movieDao.replaceCastSnapshot(movieId = id, cast = cast)
            castNames = cast.joinToString(",") { it.name }
        } catch (error: Exception) {
            enrichmentError = error
        }

        try {
            val images = api.getMovieImages(id)
            movieDao.replaceImageSnapshot(
                movieId = id,
                images = images.posters.map { it.toEntity(id, "poster") } +
                    images.backdrops.map { it.toEntity(id, "backdrop") },
            )
            posterPath = images.posters.firstOrNull()?.filePath ?: posterPath
            backdropPath = images.backdrops.firstOrNull()?.filePath ?: backdropPath
        } catch (error: Exception) {
            if (enrichmentError == null) {
                enrichmentError = error
            }
        }

        val currentMovie = movieDao.getMovie(id) ?: baseMovie
        movieDao.upsertMovie(
            currentMovie.copy(
                posterPath = posterPath,
                backdropPath = backdropPath,
                castNames = castNames,
            )
        )

        enrichmentError?.let { throw it }
    }

    override suspend fun syncFavorites() {
        if (!isLoggedIn()) return
        val favorites = api.getFavorites()
        val remoteIds = favorites.map { it.imdbId }.distinct()
        movieDao.replaceFavoriteSnapshot(
            movies = mapRemoteMovieMarks(movies = favorites, favorite = true),
            movieIds = remoteIds,
        )
    }

    override suspend fun syncWatchlist() {
        if (!isLoggedIn()) return
        val watchlist = api.getWatchlist()
        val remoteIds = watchlist.map { it.imdbId }.distinct()
        movieDao.replaceWatchlistSnapshot(
            movies = mapRemoteMovieMarks(movies = watchlist, watchlist = true),
            movieIds = remoteIds,
        )
    }

    override suspend fun getQuizPool(limit: Int): List<MovieDetails> {
        var pool = movieDao.getQuizPool(limit)

        if (pool.size < 10) {
            refreshCatalog(
                MovieCatalogQuery(
                    page = 1,
                    pageSize = 100,
                    sortBy = "imdb_votes",
                    sortOrder = "desc",
                )
            )
            pool = movieDao.getQuizPool(limit)
        }

        pool.take(30).forEach { movie ->
            if (movie.castNames.isBlank() || movie.backdropPath.isNullOrBlank()) {
                runCatching { refreshMovieDetails(movie.imdbId) }
            }
        }

        return movieDao.getQuizPool(limit).map { it.toDetails() }
    }

    override suspend fun setFavorite(movieId: String, isFavorite: Boolean) {
        val previousValue = movieDao.getMovie(movieId)?.isFavorite ?: false
        check(isLoggedIn()) { "Korisnik nije prijavljen." }
        movieDao.updateFavorite(movieId, isFavorite)

        try {
            if (isFavorite) api.addFavorite(movieId) else api.removeFavorite(movieId)
        } catch (error: Exception) {
            if (isLoggedIn()) {
                movieDao.updateFavorite(movieId, previousValue)
            } else {
                movieDao.clearUserMovieMarks()
            }
            throw error
        }
    }

    override suspend fun setWatchlisted(movieId: String, isWatchlisted: Boolean) {
        val previousValue = movieDao.getMovie(movieId)?.isWatchlisted ?: false
        check(isLoggedIn()) { "Korisnik nije prijavljen." }
        movieDao.updateWatchlist(movieId, isWatchlisted)

        try {
            if (isWatchlisted) api.addWatchlist(movieId) else api.removeWatchlist(movieId)
        } catch (error: Exception) {
            if (isLoggedIn()) {
                movieDao.updateWatchlist(movieId, previousValue)
            } else {
                movieDao.clearUserMovieMarks()
            }
            throw error
        }
    }

    private suspend fun mapRemoteMovieMarks(
        movies: List<MovieListItemApiModel>,
        favorite: Boolean = false,
        watchlist: Boolean = false,
    ) = movies.map { item ->
        val oldMovie = movieDao.getMovie(item.imdbId)
        val fresh = item.toEntity(previous = oldMovie)
        fresh.copy(
            isFavorite = favorite || oldMovie?.isFavorite == true,
            isWatchlisted = watchlist || oldMovie?.isWatchlisted == true,
        )
    }

    private suspend fun isLoggedIn(): Boolean {
        return !authStore.authData.first().token.isNullOrBlank()
    }
}
