package rs.edu.raf.showtime.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val runtime: Int? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val genres: String = "",
    val castNames: String = "",
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val isFavorite: Boolean = false,
    val isWatchlisted: Boolean = false,
)
