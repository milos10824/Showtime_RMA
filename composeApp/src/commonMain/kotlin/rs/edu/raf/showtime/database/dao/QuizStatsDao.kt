package rs.edu.raf.showtime.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.database.entity.QuizStatsEntity

@Dao
interface QuizStatsDao {

    @Query("SELECT * FROM quiz_stats WHERE id = :id")
    fun observeStats(id: Int): Flow<QuizStatsEntity?>

    @Query("SELECT * FROM quiz_stats WHERE id = :id")
    suspend fun getStats(id: Int): QuizStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: QuizStatsEntity)
}
