package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieImagesApiModel(
    val posters: List<MovieImageApiModel> = emptyList(),
    val backdrops: List<MovieImageApiModel> = emptyList(),
    val logos: List<MovieImageApiModel> = emptyList(),
)

@Serializable
data class MovieImageApiModel(
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Double? = null,
    val language: String? = null,
)