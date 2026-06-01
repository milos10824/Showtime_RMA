package rs.edu.raf.showtime.data.quiz

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.domain.quiz.QuizStats

interface QuizRepository {
    fun observeStats(): Flow<QuizStats>
    suspend fun getQuizMovies(limit: Int): List<MovieListItem>
    suspend fun saveResult(score: Int)
}
