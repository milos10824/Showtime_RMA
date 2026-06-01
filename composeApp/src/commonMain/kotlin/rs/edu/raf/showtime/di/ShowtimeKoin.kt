package rs.edu.raf.showtime.di

import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import rs.edu.raf.showtime.core.auth.di.authStorageModule
import rs.edu.raf.showtime.data.auth.di.authRepositoryModule
import rs.edu.raf.showtime.data.movie.di.movieRepositoryModule
import rs.edu.raf.showtime.data.quiz.di.quizRepositoryModule
import rs.edu.raf.showtime.database.di.databaseModule
import rs.edu.raf.showtime.networking.di.networkingModule

fun initShowtimeKoin(): Koin {
    val startedKoin = GlobalContext.getOrNull()
    if (startedKoin != null) return startedKoin

    return startKoin {
        modules(
            networkingModule,
            authStorageModule,
            databaseModule,
            movieRepositoryModule,
            authRepositoryModule,
            quizRepositoryModule,
        )
    }.koin
}
