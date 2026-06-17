package rs.edu.raf.showtime.core.auth

import rs.edu.raf.showtime.database.dao.MovieDao

class SessionManager(
    private val authStore: AuthStore,
    private val movieDao: MovieDao,
) {
    suspend fun clearSession() {
        movieDao.clearUserMovieMarks()
        authStore.clear()
    }
}
