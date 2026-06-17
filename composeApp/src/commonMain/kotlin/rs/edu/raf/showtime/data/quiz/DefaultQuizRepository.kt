package rs.edu.raf.showtime.data.quiz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
            val ownerUsername = ownerUsername(authData.username)
            flow {
                quizStatsDao.claimLegacyStats(
                    ownerUsername = ownerUsername,
                    legacyOwnerUsername = legacyQuizStatsOwnerUsername(ownerUsername),
                )
                emitAll(
                    quizStatsDao.observeStats(ownerUsername).map { stats ->
                        QuizStats(
                            bestScore = stats?.bestScore ?: 0.0,
                            playedCount = stats?.playedCount ?: 0,
                        )
                    }
                )
            }
        }
    }

    override suspend fun getQuizMovies(limit: Int): List<MovieDetails> {
        return movieRepository.getQuizPool(limit)
    }

    override suspend fun saveResult(score: Double) {
        val ownerUsername = currentOwnerUsername()
        val current = quizStatsDao.claimLegacyStats(
            ownerUsername = ownerUsername,
            legacyOwnerUsername = legacyQuizStatsOwnerUsername(ownerUsername),
        ) ?: quizStatsDao.getStats(ownerUsername)

        val newStats = QuizStatsEntity(
            id = current?.id ?: 0,
            ownerUsername = ownerUsername,
            bestScore = maxOf(current?.bestScore ?: 0.0, score),
            playedCount = (current?.playedCount ?: 0) + 1,
        )

        quizStatsDao.upsertStats(newStats)

        if (authStore.authData.first().token == null) return
        try {
            api.submitQuizResult(score)
        } catch (_: Exception) {
            // Lokalna statistika ostaje sacuvana i kada server trenutno nije dostupan.
        }
    }

    private suspend fun currentOwnerUsername(): String {
        val username = authStore.authData.first().username
        return ownerUsername(username)
    }

    private fun ownerUsername(username: String?): String {
        return username?.takeIf { it.isNotBlank() } ?: "guest"
    }

}

internal fun legacyQuizStatsOwnerUsername(ownerUsername: String): String {
    return "legacy_${ownerUsername.hashCode()}"
}
