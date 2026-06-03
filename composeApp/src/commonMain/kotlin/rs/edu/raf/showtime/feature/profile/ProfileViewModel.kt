package rs.edu.raf.showtime.feature.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.data.quiz.QuizRepository

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
    private val quizRepository: QuizRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        scope.launch {
            authRepository.authData.collect { authData ->
                _state.value = _state.value.copy(authData = authData)
            }
        }
        scope.launch {
            movieRepository.observeFavoriteCount().collect { count ->
                _state.value = _state.value.copy(favoriteCount = count)
            }
        }
        scope.launch {
            movieRepository.observeWatchlistCount().collect { count ->
                _state.value = _state.value.copy(watchlistCount = count)
            }
        }
        scope.launch {
            quizRepository.observeStats().collect { stats ->
                _state.value = _state.value.copy(
                    bestScore = stats.bestScore,
                    playedCount = stats.playedCount,
                )
            }
        }

        refresh()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> refresh()
            ProfileIntent.Logout -> logout()
        }
    }

    private fun refresh() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                authRepository.refreshProfile()
                movieRepository.syncFavorites()
                movieRepository.syncWatchlist()
                _state.value = _state.value.copy(isLoading = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Profil nije osvežen. Prikazani su lokalni podaci.",
                )
            }
        }
    }

    private fun logout() {
        scope.launch {
            movieRepository.clearUserMovieData()
            authRepository.logout()
            _effect.send(ProfileEffect.LoggedOut)
        }
    }
}
