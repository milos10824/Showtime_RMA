package rs.edu.raf.showtime.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase> {
    val appDirectory = File(System.getProperty("user.home"), ".showtime_rma")
    appDirectory.mkdirs()

    val databaseFile = File(appDirectory, "showtime.db")

    return Room.databaseBuilder<ShowtimeDatabase>(
        name = databaseFile.absolutePath,
    )
}
