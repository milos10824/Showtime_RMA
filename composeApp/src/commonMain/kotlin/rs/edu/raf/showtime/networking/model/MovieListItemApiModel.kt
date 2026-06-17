package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MovieListItemApiModel(
    @JsonNames("imdb_id")
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    @JsonNames("imdb_rating")
    val imdbRating: Double? = null,
    @JsonNames("imdb_votes")
    val imdbVotes: Int? = null,
    @JsonNames("tmdb_rating")
    val tmdbRating: Double? = null,
    @JsonNames("poster_path")
    val posterPath: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
)
