package rs.edu.raf.showtime.feature.auth

data class AuthState(
    val isSignup: Boolean = false,
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthIntent {
    data class FullNameChanged(val value: String) : AuthIntent
    data class UsernameChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data object ChangeMode : AuthIntent
    data object Submit : AuthIntent
    data object ClearError : AuthIntent
}

sealed interface AuthEffect {
    data object OpenHome : AuthEffect
}
