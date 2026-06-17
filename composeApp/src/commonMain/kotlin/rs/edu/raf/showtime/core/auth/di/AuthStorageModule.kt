package rs.edu.raf.showtime.core.auth.di

import org.koin.dsl.module
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.core.auth.SessionManager
import rs.edu.raf.showtime.core.auth.createPlatformAuthDataStore

val authStorageModule = module {
    single {
        createPlatformAuthDataStore()
    }

    single {
        AuthStore(dataStore = get())
    }

    single {
        SessionManager(
            authStore = get(),
            movieDao = get(),
        )
    }
}
