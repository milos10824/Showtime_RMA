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

    fun loading(state: AuthState): AuthState = state.copy(isLoading = true, error = null)

    fun error(state: AuthState, message: String): AuthState = state.copy(isLoading = false, error = message)

    fun idle(state: AuthState): AuthState = state.copy(isLoading = false)
}
