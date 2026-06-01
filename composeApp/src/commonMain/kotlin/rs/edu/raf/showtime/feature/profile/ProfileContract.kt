package rs.edu.raf.showtime.feature.profile

import rs.edu.raf.showtime.core.auth.AuthData

data class ProfileState(
    val authData: AuthData = AuthData(),
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
)

sealed interface ProfileIntent {
    data object Logout : ProfileIntent
}

sealed interface ProfileEffect {
    data object LoggedOut : ProfileEffect
}
