package rs.edu.raf.showtime.data.auth.di

import org.koin.dsl.module
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.data.auth.DefaultAuthRepository

val authRepositoryModule = module {
    single<AuthRepository> {
        DefaultAuthRepository(
            api = get(),
            authStore = get(),
            sessionManager = get(),
        )
    }
}
