package rs.edu.raf.showtime.feature.profile

import rs.edu.raf.showtime.core.auth.AuthData

data class ProfileState(
    val authData: AuthData = AuthData(),
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data object Logout : ProfileIntent
}

sealed interface ProfileEffect {
    data object LoggedOut : ProfileEffect
}
