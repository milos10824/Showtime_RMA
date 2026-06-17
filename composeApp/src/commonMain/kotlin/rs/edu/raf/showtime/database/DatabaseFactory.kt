package rs.edu.raf.showtime.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

expect fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase>

fun createShowtimeDatabase(
    builder: RoomDatabase.Builder<ShowtimeDatabase>,
): ShowtimeDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        .build()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE movies ADD COLUMN tmdbRating REAL")
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS catalog_entries (
                queryKey TEXT NOT NULL,
                page INTEGER NOT NULL,
                movieId TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(queryKey, page, movieId)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS catalog_pages (
                queryKey TEXT NOT NULL,
                page INTEGER NOT NULL,
                pageSize INTEGER NOT NULL,
                totalItems INTEGER NOT NULL,
                totalPages INTEGER NOT NULL,
                PRIMARY KEY(queryKey, page)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS genres (
                id INTEGER NOT NULL,
                name TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quiz_stats_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ownerUsername TEXT NOT NULL,
                bestScore REAL NOT NULL,
                playedCount INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO quiz_stats_new (ownerUsername, bestScore, playedCount)
            SELECT 'legacy_' || id, bestScore, playedCount FROM quiz_stats
            """.trimIndent()
        )
        connection.execSQL("DROP TABLE quiz_stats")
        connection.execSQL("ALTER TABLE quiz_stats_new RENAME TO quiz_stats")
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_quiz_stats_ownerUsername ON quiz_stats (ownerUsername)"
        )
    }
}
