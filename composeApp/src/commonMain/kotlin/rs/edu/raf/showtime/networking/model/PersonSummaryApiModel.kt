package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonSummaryApiModel(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)