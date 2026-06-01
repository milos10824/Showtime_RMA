package rs.edu.raf.showtime.data.quiz

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.database.dao.QuizStatsDao
import rs.edu.raf.showtime.database.entity.QuizStatsEntity
import rs.edu.raf.showtime.domain.movie.MovieListItem
import rs.edu.raf.showtime.domain.quiz.QuizStats

class DefaultQuizRepository(
    private val movieRepository: MovieRepository,
    private val quizStatsDao: QuizStatsDao,
) : QuizRepository {

    override fun observeStats(): Flow<QuizStats> {
        return quizStatsDao.observeStats().map { stats ->
            QuizStats(
                bestScore = stats?.bestScore?.toInt() ?: 0,
                playedCount = stats?.playedCount ?: 0,
            )
        }
    }

    override suspend fun getQuizMovies(limit: Int): List<MovieListItem> {
        return movieRepository.getQuizPool(limit)
    }

    override suspend fun saveResult(score: Int) {
        val current = quizStatsDao.getStats()
        val newStats = QuizStatsEntity(
            bestScore = maxOf(current?.bestScore ?: 0.0, score.toDouble()),
            playedCount = (current?.playedCount ?: 0) + 1,
        )
        quizStatsDao.upsertStats(newStats)
    }
}
