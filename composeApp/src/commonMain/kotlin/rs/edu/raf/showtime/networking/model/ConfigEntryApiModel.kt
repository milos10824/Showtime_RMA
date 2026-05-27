package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfigEntryApiModel(
    val key: String,
    val value: String,
)
