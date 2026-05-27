package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkErrorApiModel(
    val error: String? = null,
    val httpCode: Int? = null,
    val message: String? = null,
    val description: String? = null,
    val suggestion: String? = null,
)
