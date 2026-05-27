package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieApiModel(
    @SerialName("imdb_id") val imdbId: String,
    @SerialName("tmdb_id") val tmdbId: Int? = null,
    val title: String,
    @SerialName("original_title") val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    @SerialName("language_code") val languageCode: String? = null,
    val popularity: Double? = null,
    @SerialName("imdb_rating") val imdbRating: Double? = null,
    @SerialName("imdb_votes") val imdbVotes: Int? = null,
    @SerialName("tmdb_rating") val tmdbRating: Double? = null,
    @SerialName("tmdb_votes") val tmdbVotes: Int? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
    val collection: MovieCollectionApiModel? = null,
)

@Serializable
data class MovieCollectionApiModel(
    val id: Int,
    val name: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
)
