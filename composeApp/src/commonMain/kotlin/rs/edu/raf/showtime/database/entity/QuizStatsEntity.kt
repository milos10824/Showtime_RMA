package rs.edu.raf.showtime.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
)
