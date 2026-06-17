package rs.edu.raf.showtime.feature.profile

import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.domain.quiz.QuizStats

object ProfileReducer {

    fun authChanged(state: ProfileState, authData: AuthData): ProfileState {
        return state.copy(authData = authData)
    }

    fun favoriteCountChanged(state: ProfileState, count: Int): ProfileState {
        return state.copy(favoriteCount = count)
    }

    fun watchlistCountChanged(state: ProfileState, count: Int): ProfileState {
        return state.copy(watchlistCount = count)
    }

    fun statsChanged(state: ProfileState, stats: QuizStats): ProfileState {
        return state.copy(
            bestScore = stats.bestScore,
            playedCount = stats.playedCount,
        )
    }

    fun loading(state: ProfileState): ProfileState {
        return state.copy(isLoading = true, isOffline = false, error = null)
    }

    fun idle(state: ProfileState): ProfileState {
        return state.copy(isLoading = false, isOffline = false, error = null)
    }

    fun error(state: ProfileState, message: String): ProfileState {
        return state.copy(isLoading = false, error = message)
    }

    fun offline(state: ProfileState, message: String): ProfileState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }
}
