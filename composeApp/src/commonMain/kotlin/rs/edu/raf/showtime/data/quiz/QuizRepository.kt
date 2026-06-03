package rs.edu.raf.showtime.data.quiz

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.quiz.QuizStats

interface QuizRepository {
    fun observeStats(): Flow<QuizStats>
    suspend fun getQuizMovies(limit: Int): List<MovieDetails>
    suspend fun saveResult(score: Double)
}
