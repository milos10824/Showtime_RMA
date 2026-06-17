package rs.edu.raf.showtime.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.database.entity.CastMemberEntity
import rs.edu.raf.showtime.database.entity.CatalogEntryEntity
import rs.edu.raf.showtime.database.entity.CatalogPageEntity
import rs.edu.raf.showtime.database.entity.GenreEntity
import rs.edu.raf.showtime.database.entity.MovieEntity
import rs.edu.raf.showtime.database.entity.MovieImageEntity

@Dao
interface MovieDao {

    @Query(
        """
        SELECT movies.* FROM catalog_entries
        INNER JOIN movies ON movies.imdbId = catalog_entries.movieId
        WHERE catalog_entries.queryKey = :queryKey AND catalog_entries.page = :page
        ORDER BY catalog_entries.position
        """
    )
    fun observeCatalogMovies(queryKey: String, page: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM catalog_pages WHERE queryKey = :queryKey AND page = :page")
    fun observeCatalogPage(queryKey: String, page: Int): Flow<CatalogPageEntity?>

    @Query("SELECT * FROM genres ORDER BY name")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Query("SELECT * FROM movies WHERE imdbId = :id LIMIT 1")
    fun observeMovie(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE imdbId = :id LIMIT 1")
    suspend fun getMovie(id: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY title")
    fun observeFavorites(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isWatchlisted = 1 ORDER BY title")
    fun observeWatchlist(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isWatchlisted = 1")
    fun observeWatchlistCount(): Flow<Int>

    @Query("SELECT * FROM movies WHERE posterPath IS NOT NULL OR backdropPath IS NOT NULL ORDER BY imdbVotes DESC LIMIT :limit")
    suspend fun getQuizPool(limit: Int): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImages(images: List<MovieImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCast(cast: List<CastMemberEntity>)

    @Query("DELETE FROM cast_members WHERE movieId = :movieId")
    suspend fun deleteCastForMovie(movieId: String)

    @Query("DELETE FROM movie_images WHERE movieId = :movieId")
    suspend fun deleteImagesForMovie(movieId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalogEntries(entries: List<CatalogEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalogPage(page: CatalogPageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM catalog_entries WHERE queryKey = :queryKey AND page = :page")
    suspend fun deleteCatalogEntries(queryKey: String, page: Int)

    @Query("SELECT * FROM movie_images WHERE movieId = :movieId")
    fun observeImagesForMovie(movieId: String): Flow<List<MovieImageEntity>>

    @Query("SELECT * FROM cast_members WHERE movieId = :movieId")
    fun observeCastForMovie(movieId: String): Flow<List<CastMemberEntity>>

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE imdbId = :movieId")
    suspend fun updateFavorite(movieId: String, isFavorite: Boolean)

    @Query("UPDATE movies SET isWatchlisted = :isWatchlisted WHERE imdbId = :movieId")
    suspend fun updateWatchlist(movieId: String, isWatchlisted: Boolean)

    @Query("UPDATE movies SET isFavorite = 0")
    suspend fun clearFavorites()

    @Query("UPDATE movies SET isFavorite = 1 WHERE imdbId IN (:movieIds)")
    suspend fun markFavorites(movieIds: List<String>)

    @Query("UPDATE movies SET isWatchlisted = 0")
    suspend fun clearWatchlist()

    @Query("UPDATE movies SET isWatchlisted = 1 WHERE imdbId IN (:movieIds)")
    suspend fun markWatchlist(movieIds: List<String>)

    @Query("UPDATE movies SET isFavorite = 0, isWatchlisted = 0")
    suspend fun clearUserMovieMarks()

    @Transaction
    suspend fun replaceCatalogPage(
        page: CatalogPageEntity,
        entries: List<CatalogEntryEntity>,
        movies: List<MovieEntity>,
    ) {
        upsertMovies(movies)
        deleteCatalogEntries(page.queryKey, page.page)
        if (entries.isNotEmpty()) {
            upsertCatalogEntries(entries)
        }
        upsertCatalogPage(page)
    }

    @Transaction
    suspend fun replaceFavoriteSnapshot(
        movies: List<MovieEntity>,
        movieIds: List<String>,
    ) {
        upsertMovies(movies)
        clearFavorites()
        if (movieIds.isNotEmpty()) {
            markFavorites(movieIds)
        }
    }

    @Transaction
    suspend fun replaceWatchlistSnapshot(
        movies: List<MovieEntity>,
        movieIds: List<String>,
    ) {
        upsertMovies(movies)
        clearWatchlist()
        if (movieIds.isNotEmpty()) {
            markWatchlist(movieIds)
        }
    }

    @Transaction
    suspend fun replaceCastSnapshot(
        movieId: String,
        cast: List<CastMemberEntity>,
    ) {
        deleteCastForMovie(movieId)
        if (cast.isNotEmpty()) {
            upsertCast(cast)
        }
    }

    @Transaction
    suspend fun replaceImageSnapshot(
        movieId: String,
        images: List<MovieImageEntity>,
    ) {
        deleteImagesForMovie(movieId)
        if (images.isNotEmpty()) {
            upsertImages(images)
        }
    }
}
