package rs.edu.raf.showtime.database.entity

import androidx.room.Entity

@Entity(
    tableName = "catalog_entries",
    primaryKeys = ["queryKey", "page", "movieId"],
)
data class CatalogEntryEntity(
    val queryKey: String,
    val page: Int,
    val movieId: String,
    val position: Int,
)
