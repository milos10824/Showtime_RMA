package rs.edu.raf.showtime.feature.profile

import rs.edu.raf.showtime.core.auth.AuthData

data class ProfileState(
    val authData: AuthData = AuthData(),
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && authData.username.isNullOrBlank()
}

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data object Logout : ProfileIntent
    data object BackClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data object Close : ProfileEffect
}
