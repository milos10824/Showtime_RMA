package rs.edu.raf.showtime.data.auth

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.networking.ShowtimeApi

class DefaultAuthRepository(
    private val api: ShowtimeApi,
    private val authStore: AuthStore,
) : AuthRepository {

    override val authData: Flow<AuthData> = authStore.authData

    override suspend fun login(username: String, password: String) {
        val response = api.login(username = username, password = password)
        authStore.saveAuthData(
            token = response.accessToken,
            username = response.user?.username ?: username,
            fullName = response.user?.fullName,
        )
    }

    override suspend fun signup(fullName: String, username: String, password: String) {
        val response = api.signup(fullName = fullName, username = username, password = password)
        authStore.saveAuthData(
            token = response.accessToken,
            username = response.user?.username ?: username,
            fullName = response.user?.fullName ?: fullName,
        )
    }

    override suspend fun refreshProfile() {
        val currentAuth = authStore.authData.first()
        val token = currentAuth.token ?: return

        try {
            val profile = api.getProfile(token)
            authStore.saveAuthData(
                token = token,
                username = profile.username,
                fullName = profile.fullName,
            )
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
            }
            throw e
        }
    }

    override suspend fun logout() {
        authStore.clear()
    }
}
