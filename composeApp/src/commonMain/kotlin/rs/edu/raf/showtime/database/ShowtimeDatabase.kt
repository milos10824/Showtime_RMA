package rs.edu.raf.showtime.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import rs.edu.raf.showtime.database.dao.MovieDao
import rs.edu.raf.showtime.database.dao.QuizStatsDao
import rs.edu.raf.showtime.database.entity.CastMemberEntity
import rs.edu.raf.showtime.database.entity.CatalogEntryEntity
import rs.edu.raf.showtime.database.entity.CatalogPageEntity
import rs.edu.raf.showtime.database.entity.GenreEntity
import rs.edu.raf.showtime.database.entity.MovieEntity
import rs.edu.raf.showtime.database.entity.MovieImageEntity
import rs.edu.raf.showtime.database.entity.QuizStatsEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieImageEntity::class,
        CastMemberEntity::class,
        CatalogEntryEntity::class,
        CatalogPageEntity::class,
        GenreEntity::class,
        QuizStatsEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(ShowtimeDatabaseConstructor::class)
abstract class ShowtimeDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun quizStatsDao(): QuizStatsDao
}

@Suppress("KotlinNoActualForExpect")
expect object ShowtimeDatabaseConstructor : RoomDatabaseConstructor<ShowtimeDatabase> {
    override fun initialize(): ShowtimeDatabase
}
