package rs.edu.raf.showtime.data.movie.mapper

import rs.edu.raf.showtime.database.entity.CastMemberEntity
import rs.edu.raf.showtime.database.entity.MovieEntity
import rs.edu.raf.showtime.database.entity.MovieImageEntity
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.networking.model.MovieApiModel
import rs.edu.raf.showtime.networking.model.MovieImageApiModel
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel
import rs.edu.raf.showtime.networking.model.PersonSummaryApiModel

fun MovieListItemApiModel.toEntity(previous: MovieEntity? = null): MovieEntity {
    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        runtime = previous?.runtime,
        overview = previous?.overview,
        posterPath = posterPath,
        backdropPath = previous?.backdropPath,
        genres = genres.joinToString(",") { it.name },
        castNames = previous?.castNames.orEmpty(),
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating ?: previous?.tmdbRating,
        isFavorite = previous?.isFavorite ?: false,
        isWatchlisted = previous?.isWatchlisted ?: false,
    )
}

fun MovieApiModel.toEntity(previous: MovieEntity?): MovieEntity {
    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        runtime = runtime,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        genres = genres.joinToString(",") { it.name },
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        isFavorite = previous?.isFavorite ?: false,
        isWatchlisted = previous?.isWatchlisted ?: false,
    )
}

fun MovieEntity.toListItem(): MovieListItem {
    return MovieListItem(
        imdbId = imdbId,
        title = title,
        year = year,
        posterPath = posterPath,
        genres = genres.split(",").map { it.trim() }.filter { it.isNotBlank() },
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        isFavorite = isFavorite,
        isWatchlisted = isWatchlisted,
    )
}

fun MovieEntity.toDetails(): MovieDetails {
    return MovieDetails(
        imdbId = imdbId,
        title = title,
        year = year,
        runtime = runtime,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        genres = genres.split(",").map { it.trim() }.filter { it.isNotBlank() },
        castNames = castNames.split(",").map { it.trim() }.filter { it.isNotBlank() },
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        isFavorite = isFavorite,
        isWatchlisted = isWatchlisted,
    )
}

fun PersonSummaryApiModel.toEntity(movieId: String): CastMemberEntity {
    return CastMemberEntity(
        movieId = movieId,
        personId = imdbId,
        name = name,
        profilePath = profilePath,
    )
}

fun PersonSummaryApiModel.isActor(): Boolean {
    if (department.equals("Acting", ignoreCase = true)) {
        return true
    }

    return professions
        ?.split(",")
        ?.any { profession ->
            profession.trim().equals("actor", ignoreCase = true) ||
                profession.trim().equals("actress", ignoreCase = true)
        }
        ?: false
}

fun MovieImageApiModel.toEntity(movieId: String, type: String): MovieImageEntity {
    return MovieImageEntity(
        movieId = movieId,
        filePath = filePath,
        type = type,
        width = width,
        height = height,
    )
}
