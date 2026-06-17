package rs.edu.raf.showtime.feature.session

import rs.edu.raf.showtime.core.auth.AuthData

data class AppSessionState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
)

sealed interface AppSessionAction {
    data class AuthChanged(val authData: AuthData) : AppSessionAction
    data object SyncFailed : AppSessionAction
}

sealed interface AppSessionEffect {
    data object OpenAuth : AppSessionEffect
}

object AppSessionReducer {
    fun reduce(state: AppSessionState, action: AppSessionAction): AppSessionState {
        return when (action) {
            is AppSessionAction.AuthChanged -> state.copy(
                isLoading = false,
                isLoggedIn = action.authData.isLoggedIn,
                isOffline = false,
                error = null,
            )
            AppSessionAction.SyncFailed -> state.copy(
                isOffline = true,
                error = "Korisničke liste nisu osvežene. Prikazujem lokalne podatke.",
            )
        }
    }
}
