package rs.edu.raf.showtime.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.data.auth.AuthRepository
import rs.edu.raf.showtime.feature.saved.SavedListType

data class HomeState(
    val authData: AuthData = AuthData(),
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && authData.username.isNullOrBlank()
}

sealed interface HomeIntent {
    data object OpenCatalog : HomeIntent
    data class OpenSaved(val type: SavedListType) : HomeIntent
    data object OpenProfile : HomeIntent
    data object OpenQuiz : HomeIntent
}

sealed interface HomeEffect {
    data object OpenCatalog : HomeEffect
    data class OpenSaved(val type: SavedListType) : HomeEffect
    data object OpenProfile : HomeEffect
    data object OpenQuiz : HomeEffect
}

object HomeReducer {
    fun authChanged(state: HomeState, authData: AuthData): HomeState {
        return state.copy(authData = authData)
    }

    fun loading(state: HomeState): HomeState {
        return state.copy(isLoading = true, isOffline = false, error = null)
    }

    fun idle(state: HomeState): HomeState {
        return state.copy(isLoading = false, isOffline = false, error = null)
    }

    fun error(state: HomeState, message: String): HomeState {
        return state.copy(isLoading = false, isOffline = false, error = message)
    }

    fun offline(state: HomeState, message: String): HomeState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }
}

class HomeViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val events = MutableSharedFlow<HomeIntent>()
    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
        viewModelScope.launch {
            authRepository.authData.collect { authData ->
                _state.value = HomeReducer.authChanged(_state.value, authData)
            }
        }
        refreshProfile()
    }

    fun onIntent(intent: HomeIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OpenCatalog -> viewModelScope.launch { _effect.emit(HomeEffect.OpenCatalog) }
            is HomeIntent.OpenSaved -> viewModelScope.launch {
                _effect.emit(HomeEffect.OpenSaved(intent.type))
            }
            HomeIntent.OpenProfile -> viewModelScope.launch { _effect.emit(HomeEffect.OpenProfile) }
            HomeIntent.OpenQuiz -> viewModelScope.launch { _effect.emit(HomeEffect.OpenQuiz) }
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            _state.value = HomeReducer.loading(_state.value)
            try {
                authRepository.refreshProfile()
                _state.value = HomeReducer.idle(_state.value)
            } catch (error: Exception) {
                _state.value = if (error is ClientRequestException) {
                    HomeReducer.error(_state.value, "Server nije osvežio podatke korisnika.")
                } else {
                    HomeReducer.offline(
                        state = _state.value,
                        message = "Nema mreže. Prikazujem lokalno sačuvane podatke korisnika.",
                    )
                }
            }
        }
    }
}
