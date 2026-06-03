package rs.edu.raf.showtime.data.quiz.di

import org.koin.dsl.module
import rs.edu.raf.showtime.data.quiz.DefaultQuizRepository
import rs.edu.raf.showtime.data.quiz.QuizRepository

val quizRepositoryModule = module {
    single<QuizRepository> {
        DefaultQuizRepository(
            movieRepository = get(),
            quizStatsDao = get(),
            api = get(),
            authStore = get(),
        )
    }
}
