package rs.edu.raf.showtime.database.entity

import androidx.room.Entity

@Entity(
    tableName = "movie_images",
    primaryKeys = ["movieId", "filePath"]
)
data class MovieImageEntity(
    val movieId: String,
    val filePath: String,
    val type: String,
    val width: Int? = null,
    val height: Int? = null,
)
