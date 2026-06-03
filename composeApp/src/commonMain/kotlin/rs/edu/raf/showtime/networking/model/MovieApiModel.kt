package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MovieApiModel(
    @JsonNames("imdb_id")
    val imdbId: String,
    @JsonNames("tmdb_id")
    val tmdbId: Int? = null,
    val title: String,
    @JsonNames("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    @JsonNames("release_date")
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    @JsonNames("language_code")
    val languageCode: String? = null,
    val popularity: Double? = null,
    @JsonNames("imdb_rating")
    val imdbRating: Double? = null,
    @JsonNames("imdb_votes")
    val imdbVotes: Int? = null,
    @JsonNames("tmdb_rating")
    val tmdbRating: Double? = null,
    @JsonNames("tmdb_votes")
    val tmdbVotes: Int? = null,
    @JsonNames("poster_path")
    val posterPath: String? = null,
    @JsonNames("backdrop_path")
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
    val collection: MovieCollectionApiModel? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MovieCollectionApiModel(
    val id: Int,
    val name: String,
    @JsonNames("poster_path")
    val posterPath: String? = null,
    @JsonNames("backdrop_path")
    val backdropPath: String? = null,
)
