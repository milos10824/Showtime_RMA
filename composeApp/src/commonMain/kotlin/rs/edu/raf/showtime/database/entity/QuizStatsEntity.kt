package rs.edu.raf.showtime.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_stats",
    indices = [Index(value = ["ownerUsername"], unique = true)],
)
data class QuizStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUsername: String,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
)
