package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MovieImagesApiModel(
    val posters: List<MovieImageApiModel> = emptyList(),
    val backdrops: List<MovieImageApiModel> = emptyList(),
    val logos: List<MovieImageApiModel> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MovieImageApiModel(
    @JsonNames("file_path")
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    @JsonNames("vote_average")
    val voteAverage: Double? = null,
    val language: String? = null,
)
