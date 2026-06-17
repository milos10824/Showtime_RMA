package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.feature.session.AppSessionAction
import rs.edu.raf.showtime.feature.session.AppSessionReducer
import rs.edu.raf.showtime.feature.session.AppSessionState

class AppSessionReducerTest {

    @Test
    fun persistedTokenResolvesSessionAsLoggedIn() {
        val result = AppSessionReducer.reduce(
            state = AppSessionState(),
            action = AppSessionAction.AuthChanged(
                AuthData(token = "token", username = "user"),
            ),
        )

        assertFalse(result.isLoading)
        assertTrue(result.isLoggedIn)
    }

    @Test
    fun syncFailureKeepsSessionAndMarksOfflineState() {
        val result = AppSessionReducer.reduce(
            state = AppSessionState(isLoading = false, isLoggedIn = true),
            action = AppSessionAction.SyncFailed,
        )

        assertTrue(result.isLoggedIn)
        assertTrue(result.isOffline)
    }
}
