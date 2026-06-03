package rs.edu.raf.showtime.domain.movie

data class MovieListItem(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val posterPath: String?,
    val genres: List<String>,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean,
)

data class MovieDetails(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val runtime: Int?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val genres: List<String>,
    val castNames: List<String>,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean,
)
