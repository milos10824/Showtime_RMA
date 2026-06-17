package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import rs.edu.raf.showtime.feature.saved.SavedListType
import rs.edu.raf.showtime.navigation.ShowtimeRoute

class ShowtimeRouteTest {

    @Test
    fun favoriteRouteUsesSpecificationNaming() {
        assertEquals("saved/favorite", ShowtimeRoute.saved(SavedListType.FAVORITE))
    }

    @Test
    fun initialRouteUsesPersistedSessionState() {
        assertEquals(ShowtimeRoute.HOME, initialShowtimeRoute(isLoggedIn = true))
        assertEquals(ShowtimeRoute.AUTH, initialShowtimeRoute(isLoggedIn = false))
    }
}
