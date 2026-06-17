package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rs.edu.raf.showtime.feature.home.HomeReducer
import rs.edu.raf.showtime.feature.home.HomeState

class HomeReducerTest {

    @Test
    fun offlineProfileRefreshIsExplicitlyRepresented() {
        val result = HomeReducer.offline(
            state = HomeState(isLoading = true),
            message = "offline",
        )

        assertFalse(result.isLoading)
        assertTrue(result.isOffline)
        assertTrue(result.error != null)
    }
}
