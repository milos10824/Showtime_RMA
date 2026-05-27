package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieImagesApiModel(
    val posters: List<MovieImageApiModel> = emptyList(),
    val backdrops: List<MovieImageApiModel> = emptyList(),
    val logos: List<MovieImageApiModel> = emptyList(),
)

@Serializable
data class MovieImageApiModel(
    @SerialName("file_path") val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("iso_639_1") val language: String? = null,
)
