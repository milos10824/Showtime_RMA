package rs.edu.raf.showtime.data.auth

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.core.auth.AuthData

interface AuthRepository {
    val authData: Flow<AuthData>

    suspend fun login(username: String, password: String)

    suspend fun signup(fullName: String, username: String, password: String)

    suspend fun refreshProfile()

    suspend fun logout()
}
