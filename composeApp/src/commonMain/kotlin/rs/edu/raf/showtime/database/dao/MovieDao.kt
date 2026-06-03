package rs.edu.raf.showtime.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.database.entity.CastMemberEntity
import rs.edu.raf.showtime.database.entity.MovieEntity
import rs.edu.raf.showtime.database.entity.MovieImageEntity

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY imdbVotes DESC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY imdbVotes DESC")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

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

    @Query("SELECT * FROM movie_images WHERE movieId = :movieId")
    suspend fun getImagesForMovie(movieId: String): List<MovieImageEntity>

    @Query("SELECT * FROM cast_members WHERE movieId = :movieId")
    suspend fun getCastForMovie(movieId: String): List<CastMemberEntity>

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE imdbId = :movieId")
    suspend fun updateFavorite(movieId: String, isFavorite: Boolean)

    @Query("UPDATE movies SET isWatchlisted = :isWatchlisted WHERE imdbId = :movieId")
    suspend fun updateWatchlist(movieId: String, isWatchlisted: Boolean)

    @Query("UPDATE movies SET isFavorite = 0")
    suspend fun clearFavorites()

    @Query("UPDATE movies SET isWatchlisted = 0")
    suspend fun clearWatchlist()

    @Query("UPDATE movies SET isFavorite = 0, isWatchlisted = 0")
    suspend fun clearUserMovieMarks()
}
