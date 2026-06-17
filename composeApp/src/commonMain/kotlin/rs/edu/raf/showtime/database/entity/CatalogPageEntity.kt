package rs.edu.raf.showtime.database.entity

import androidx.room.Entity

@Entity(
    tableName = "catalog_pages",
    primaryKeys = ["queryKey", "page"],
)
data class CatalogPageEntity(
    val queryKey: String,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
)
