package rs.edu.raf.showtime.data.movie.di

import org.koin.dsl.module
import rs.edu.raf.showtime.data.movie.DefaultMovieRepository
import rs.edu.raf.showtime.data.movie.MovieRepository

val movieRepositoryModule = module {
    single<MovieRepository> {
        DefaultMovieRepository(
            api = get(),
            movieDao = get(),
            authStore = get(),
        )
    }
}
