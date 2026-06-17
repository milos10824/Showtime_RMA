package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import rs.edu.raf.showtime.data.movie.MovieCatalogQuery

class MovieCatalogQueryTest {

    @Test
    fun cacheKeyIsSharedByPagesOfSameQuery() {
        val first = MovieCatalogQuery(page = 1, query = "matrix", genreId = 28)
        val second = MovieCatalogQuery(page = 2, query = "matrix", genreId = 28)

        assertEquals(first.cacheKey, second.cacheKey)
    }

    @Test
    fun cacheKeySeparatesDifferentFilters() {
        val action = MovieCatalogQuery(query = "matrix", genreId = 28)
        val drama = MovieCatalogQuery(query = "matrix", genreId = 18)

        assertNotEquals(action.cacheKey, drama.cacheKey)
    }
}
