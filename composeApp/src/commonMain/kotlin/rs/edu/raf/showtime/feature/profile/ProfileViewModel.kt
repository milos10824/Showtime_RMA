package rs.edu.raf.showtime.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.data.quiz.QuizRepository

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val events = MutableSharedFlow<ProfileIntent>()
    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeEvents()
        viewModelScope.launch {
            authRepository.authData.collect { authData ->
                _state.value = ProfileReducer.authChanged(_state.value, authData)
            }
        }

        viewModelScope.launch {
            movieRepository.observeFavoriteCount().collect { count ->
                _state.value = ProfileReducer.favoriteCountChanged(_state.value, count)
            }
        }

        viewModelScope.launch {
            movieRepository.observeWatchlistCount().collect { count ->
                _state.value = ProfileReducer.watchlistCountChanged(_state.value, count)
            }
        }

        viewModelScope.launch {
            quizRepository.observeStats().collect { stats ->
                _state.value = ProfileReducer.statsChanged(_state.value, stats)
            }
        }

        refresh()
    }

    fun onIntent(intent: ProfileIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Logout -> logout()
            ProfileIntent.Refresh -> refresh()
            ProfileIntent.BackClicked -> viewModelScope.launch {
                _effect.emit(ProfileEffect.Close)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = ProfileReducer.loading(_state.value)

            try {
                authRepository.refreshProfile()
                movieRepository.syncFavorites()
                movieRepository.syncWatchlist()
                _state.value = ProfileReducer.idle(_state.value)
            } catch (error: Exception) {
                _state.value = if (error is ClientRequestException) {
                    ProfileReducer.error(_state.value, "Server nije osvežio profil.")
                } else {
                    ProfileReducer.offline(
                        state = _state.value,
                        message = "Profil nije osvežen. Prikazujem lokalne podatke.",
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
