package rs.edu.raf.showtime.feature.auth

object AuthReducer {

    fun reduce(state: AuthState, intent: AuthIntent): AuthState {
        return when (intent) {
            is AuthIntent.FullNameChanged -> state.copy(fullName = intent.value, error = null)
            is AuthIntent.UsernameChanged -> state.copy(username = intent.value, error = null)
            is AuthIntent.PasswordChanged -> state.copy(password = intent.value, error = null)
            AuthIntent.ChangeMode -> state.copy(isSignup = !state.isSignup, error = null, password = "")
            AuthIntent.ClearError -> state.copy(error = null)
            AuthIntent.Submit -> state
        }
    }

    fun loading(state: AuthState): AuthState = state.copy(isLoading = true, isOffline = false, error = null)

    fun error(
        state: AuthState,
        message: String,
        isOffline: Boolean = false,
    ): AuthState = state.copy(isLoading = false, isOffline = isOffline, error = message)

    fun idle(state: AuthState): AuthState = state.copy(isLoading = false, isOffline = false)
}
