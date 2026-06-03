package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PersonSummaryApiModel(
    @JsonNames("imdb_id")
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    @JsonNames("profile_path")
    val profilePath: String? = null,
)
