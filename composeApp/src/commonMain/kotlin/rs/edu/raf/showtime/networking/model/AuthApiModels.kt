package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestApiModel(
    val username: String,
    val password: String,
)

@Serializable
data class SignupRequestApiModel(
    @SerialName("full_name")
    val fullName: String,
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponseApiModel(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
    val user: UserApiModel? = null,
)

@Serializable
data class UserApiModel(
    val id: Int? = null,
    val username: String,
    @SerialName("full_name")
    val fullName: String? = null,
)
