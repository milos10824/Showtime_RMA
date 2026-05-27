package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieListItemApiModel(
    @SerialName("imdb_id") val imdbId: String,
    val title: String,
    val year: Int? = null,
    @SerialName("imdb_rating") val imdbRating: Double? = null,
    @SerialName("imdb_votes") val imdbVotes: Int? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
)
