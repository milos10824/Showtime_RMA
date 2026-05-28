package rs.edu.raf.showtime.database

import androidx.room.Room
import androidx.room.RoomDatabase
import rs.edu.raf.showtime.core.auth.AndroidAppContext

actual fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase> {
    val context = AndroidAppContext.requireContext()
    val databaseFile = context.getDatabasePath("showtime.db")

    return Room.databaseBuilder<ShowtimeDatabase>(
        context = context,
        name = databaseFile.absolutePath,
    )
}
