package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PaginatedResponse<T>(
    val page: Int,
    @JsonNames("page_size")
    val pageSize: Int,
    @JsonNames("total_items")
    val totalItems: Int,
    @JsonNames("total_pages")
    val totalPages: Int,
    val items: List<T>,
)
