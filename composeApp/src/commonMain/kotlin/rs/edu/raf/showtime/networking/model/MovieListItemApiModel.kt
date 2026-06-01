package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieListItemApiModel(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
)