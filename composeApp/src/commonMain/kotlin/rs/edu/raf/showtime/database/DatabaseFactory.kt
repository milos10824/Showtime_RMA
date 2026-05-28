package rs.edu.raf.showtime.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

expect fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase>

fun createShowtimeDatabase(
    builder: RoomDatabase.Builder<ShowtimeDatabase>,
): ShowtimeDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
