package rs.edu.raf.showtime.data.quiz

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.database.dao.QuizStatsDao
import rs.edu.raf.showtime.database.entity.QuizStatsEntity
import rs.edu.raf.showtime.domain.movie.MovieDetails
import rs.edu.raf.showtime.domain.quiz.QuizStats
import rs.edu.raf.showtime.networking.ShowtimeApi

class DefaultQuizRepository(
    private val movieRepository: MovieRepository,
    private val quizStatsDao: QuizStatsDao,
    private val api: ShowtimeApi,
    private val authStore: AuthStore,
) : QuizRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeStats(): Flow<QuizStats> {
        return authStore.authData.flatMapLatest { authData ->
            quizStatsDao.observeStats(ownerId(authData.username)).map { stats ->
                QuizStats(
                    bestScore = stats?.bestScore ?: 0.0,
                    playedCount = stats?.playedCount ?: 0,
                )
            }
        }
    }

    override suspend fun getQuizMovies(limit: Int): List<MovieDetails> {
        return movieRepository.getQuizPool(limit)
    }

    override suspend fun saveResult(score: Double) {
        val id = currentOwnerId()
        val current = quizStatsDao.getStats(id)

        val newStats = QuizStatsEntity(
            id = id,
            bestScore = maxOf(current?.bestScore ?: 0.0, score),
            playedCount = (current?.playedCount ?: 0) + 1,
        )

        quizStatsDao.upsertStats(newStats)

        val token = authStore.authData.first().token ?: return
        try {
            api.submitQuizResult(token, score)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                authStore.clear()
            }
        } catch (_: Exception) {
            // Lokalna statistika ostaje sacuvana i kada server trenutno nije dostupan.
        }
    }

    private suspend fun currentOwnerId(): Int {
        val username = authStore.authData.first().username
        return ownerId(username)
    }

    private fun ownerId(username: String?): Int {
        return (username ?: "guest").hashCode()
    }
}
