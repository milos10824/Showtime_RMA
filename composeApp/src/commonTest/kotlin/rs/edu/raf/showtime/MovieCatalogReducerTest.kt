package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import rs.edu.raf.showtime.feature.catalog.MovieCatalogIntent
import rs.edu.raf.showtime.feature.catalog.MovieCatalogReducer
import rs.edu.raf.showtime.feature.catalog.MovieCatalogState

class MovieCatalogReducerTest {

    @Test
    fun nextPageIncrementsCurrentPage() {
        val state = MovieCatalogState(page = 2, totalPages = 5)

        val result = MovieCatalogReducer.reduce(state, MovieCatalogIntent.NextPage)

        assertEquals(3, result.page)
    }

    @Test
    fun nextPageDoesNotMovePastLastPage() {
        val state = MovieCatalogState(page = 5, totalPages = 5)

        val result = MovieCatalogReducer.reduce(state, MovieCatalogIntent.NextPage)

        assertEquals(5, result.page)
    }

    @Test
    fun previousPageNeverGoesBelowFirstPage() {
        val state = MovieCatalogState(page = 1)

        val result = MovieCatalogReducer.reduce(state, MovieCatalogIntent.PreviousPage)

        assertEquals(1, result.page)
    }

    @Test
    fun changingSearchResetsPagination() {
        val state = MovieCatalogState(page = 4)

        val result = MovieCatalogReducer.reduce(
            state = state,
            intent = MovieCatalogIntent.SearchChanged("matrix"),
        )

        assertEquals(1, result.page)
        assertEquals("matrix", result.query)
    }

    @Test
    fun navigationIntentsDoNotMutateCatalogState() {
        val state = MovieCatalogState(page = 2, totalPages = 5)

        assertEquals(
            state,
            MovieCatalogReducer.reduce(state, MovieCatalogIntent.MovieClicked("tt0133093")),
        )
        assertEquals(
            state,
            MovieCatalogReducer.reduce(state, MovieCatalogIntent.BackClicked),
        )
    }

    @Test
    fun genreRefreshFailureIsKeptSeparateFromCatalogData() {
        val state = MovieCatalogState()

        val result = MovieCatalogReducer.genresError(state, "offline")

        assertEquals("offline", result.genreError)
        assertTrue(result.movies.isEmpty())
    }
}
