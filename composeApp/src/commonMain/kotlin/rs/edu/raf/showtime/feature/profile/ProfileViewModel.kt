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

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
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
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Logout -> logout()
        }
    }

    private fun logout() {
        scope.launch {
            authRepository.logout()
            movieRepository.clearUserMovieData()
            _effect.send(ProfileEffect.LoggedOut)
        }
    }
}
