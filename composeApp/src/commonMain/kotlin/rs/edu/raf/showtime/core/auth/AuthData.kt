package rs.edu.raf.showtime.core.auth

data class AuthData(
    val token: String? = null,
    val username: String? = null,
    val fullName: String? = null,
) {
    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()
}
