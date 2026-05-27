package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class GenreApiModel(
    val id: Int,
    val name: String,
)
