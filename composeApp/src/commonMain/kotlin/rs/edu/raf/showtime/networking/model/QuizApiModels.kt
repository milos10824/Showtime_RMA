package rs.edu.raf.showtime.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizResultRequestApiModel(
    val score: Double,
    val category: Int = 1,
)
