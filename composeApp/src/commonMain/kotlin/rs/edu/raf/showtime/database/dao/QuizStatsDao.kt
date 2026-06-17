package rs.edu.raf.showtime.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.database.entity.QuizStatsEntity

@Dao
interface QuizStatsDao {

    @Query("SELECT * FROM quiz_stats WHERE ownerUsername = :ownerUsername")
    fun observeStats(ownerUsername: String): Flow<QuizStatsEntity?>

    @Query("SELECT * FROM quiz_stats WHERE ownerUsername = :ownerUsername")
    suspend fun getStats(ownerUsername: String): QuizStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: QuizStatsEntity)

    @Query("DELETE FROM quiz_stats WHERE ownerUsername = :ownerUsername")
    suspend fun deleteStats(ownerUsername: String)

    @Transaction
    suspend fun claimLegacyStats(
        ownerUsername: String,
        legacyOwnerUsername: String,
    ): QuizStatsEntity? {
        getStats(ownerUsername)?.let { return it }
        val legacy = getStats(legacyOwnerUsername) ?: return null
        deleteStats(legacyOwnerUsername)
        val claimed = legacy.copy(id = 0, ownerUsername = ownerUsername)
        upsertStats(claimed)
        return claimed
    }
}
