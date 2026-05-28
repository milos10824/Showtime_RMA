package rs.edu.raf.showtime.database.di

import org.koin.dsl.module
import rs.edu.raf.showtime.database.ShowtimeDatabase
import rs.edu.raf.showtime.database.createPlatformDatabaseBuilder
import rs.edu.raf.showtime.database.createShowtimeDatabase

val databaseModule = module {
    single {
        createShowtimeDatabase(createPlatformDatabaseBuilder())
    }

    single {
        get<ShowtimeDatabase>().movieDao()
    }

    single {
        get<ShowtimeDatabase>().quizStatsDao()
    }
}
