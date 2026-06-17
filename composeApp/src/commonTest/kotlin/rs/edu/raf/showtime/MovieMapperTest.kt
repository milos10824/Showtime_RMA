package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rs.edu.raf.showtime.data.movie.mapper.isActor
import rs.edu.raf.showtime.data.movie.mapper.toEntity
import rs.edu.raf.showtime.database.entity.MovieEntity
import rs.edu.raf.showtime.networking.model.GenreApiModel
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel
import rs.edu.raf.showtime.networking.model.PersonSummaryApiModel

class MovieMapperTest {

    @Test
    fun castFilteringKeepsActorsAndRejectsCrewMembers() {
        assertTrue(
            PersonSummaryApiModel(
                imdbId = "nm1",
                name = "Actor",
                professions = "actor,producer",
                department = "Acting",
            ).isActor()
        )
        assertTrue(
            PersonSummaryApiModel(
                imdbId = "nm2",
                name = "Actress",
                professions = "actress",
                department = null,
            ).isActor()
        )
        assertFalse(
            PersonSummaryApiModel(
                imdbId = "nm3",
                name = "Director",
                professions = "writer,director",
                department = "Directing",
            ).isActor()
        )
    }

    @Test
    fun listSnapshotPreservesLocallyCachedDetailsAndTmdbRating() {
        val previous = MovieEntity(
            imdbId = "tt1",
            title = "Old title",
            year = 1999,
            runtime = 123,
            overview = "Cached overview",
            posterPath = "/old-poster.jpg",
            backdropPath = "/cached-backdrop.jpg",
            genres = "Drama",
            castNames = "Actor One,Actor Two",
            imdbRating = 7.0,
            imdbVotes = 10,
            tmdbRating = 8.4,
            isFavorite = true,
            isWatchlisted = true,
        )
        val remote = MovieListItemApiModel(
            imdbId = "tt1",
            title = "Fresh title",
            year = 2000,
            imdbRating = 8.0,
            imdbVotes = 20,
            tmdbRating = null,
            posterPath = "/fresh-poster.jpg",
            genres = listOf(GenreApiModel(id = 18, name = "Drama")),
        )

        val entity = remote.toEntity(previous)

        assertEquals("Fresh title", entity.title)
        assertEquals(123, entity.runtime)
        assertEquals("Cached overview", entity.overview)
        assertEquals("/cached-backdrop.jpg", entity.backdropPath)
        assertEquals("Actor One,Actor Two", entity.castNames)
        assertEquals(8.4, entity.tmdbRating)
        assertTrue(entity.isFavorite)
        assertTrue(entity.isWatchlisted)
    }

    @Test
    fun listSnapshotUsesFreshTmdbRatingWhenServerProvidesIt() {
        val previous = MovieEntity(
            imdbId = "tt1",
            title = "Movie",
            tmdbRating = 7.1,
        )
        val remote = MovieListItemApiModel(
            imdbId = "tt1",
            title = "Movie",
            tmdbRating = 8.2,
        )

        assertEquals(8.2, remote.toEntity(previous).tmdbRating)
    }
}
