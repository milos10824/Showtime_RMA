package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonSummaryApiModel(
    @SerialName("imdb_id") val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
)
