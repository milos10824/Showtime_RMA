package rs.edu.raf.showtime.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.data.movie.MovieRepository

class AppSessionViewModel(
    authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppSessionState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AppSessionEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.authData.distinctUntilChangedBy { it.token }.collect { authData ->
                val wasResolved = !_state.value.isLoading
                _state.value = AppSessionReducer.reduce(
                    state = _state.value,
                    action = AppSessionAction.AuthChanged(authData),
                )

                if (authData.isLoggedIn) {
                    runCatching {
                        movieRepository.syncFavorites()
                        movieRepository.syncWatchlist()
                    }.onFailure {
                        _state.value = AppSessionReducer.reduce(
                            state = _state.value,
                            action = AppSessionAction.SyncFailed,
                        )
                    }
                } else if (wasResolved) {
                    _effect.emit(AppSessionEffect.OpenAuth)
                }
            }
        }
    }
}
