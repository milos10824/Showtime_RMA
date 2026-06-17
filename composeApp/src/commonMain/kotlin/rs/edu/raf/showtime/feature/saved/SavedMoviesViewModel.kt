package rs.edu.raf.showtime.feature.saved

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.navigation.savedListTypeOrThrow

class SavedMoviesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {
    private val type = savedStateHandle.savedListTypeOrThrow

    private val _state = MutableStateFlow(SavedMoviesState(type = type))
    val state: StateFlow<SavedMoviesState> = _state.asStateFlow()
    private val events = MutableSharedFlow<SavedMoviesIntent>()
    private val _effect = MutableSharedFlow<SavedMoviesEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeEvents()
        observeMovies()
        refresh()
    }

    fun onIntent(intent: SavedMoviesIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: SavedMoviesIntent) {
        when (intent) {
            SavedMoviesIntent.Refresh -> refresh()
            is SavedMoviesIntent.RemoveClicked -> removeMovie(intent.movieId)
            is SavedMoviesIntent.MovieClicked -> viewModelScope.launch {
                _effect.emit(SavedMoviesEffect.OpenMovieDetails(intent.movieId))
            }
            SavedMoviesIntent.BackClicked -> viewModelScope.launch {
                _effect.emit(SavedMoviesEffect.Close)
            }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            val flow = when (type) {
                SavedListType.FAVORITE -> repository.observeFavorites()
                SavedListType.WATCHLIST -> repository.observeWatchlist()
            }

            flow.collect { movies ->
                _state.value = SavedMoviesReducer.moviesLoaded(_state.value, movies)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = SavedMoviesReducer.loading(_state.value)

            try {
                when (type) {
                    SavedListType.FAVORITE -> repository.syncFavorites()
                    SavedListType.WATCHLIST -> repository.syncWatchlist()
                }

                _state.value = SavedMoviesReducer.idle(_state.value)
            } catch (error: Exception) {
                _state.value = if (error is ClientRequestException) {
                    SavedMoviesReducer.error(_state.value, "Server nije osvežio listu.")
                } else {
                    SavedMoviesReducer.offline(
                        state = _state.value,
                        message = "Lista nije osvežena. Prikazani su lokalni podaci.",
                    )
                }
            }
        }
    }

    private fun removeMovie(movieId: String) {
        viewModelScope.launch {
            runCatching {
                when (type) {
                    SavedListType.FAVORITE -> repository.setFavorite(movieId, false)
                    SavedListType.WATCHLIST -> repository.setWatchlisted(movieId, false)
                }
            }.onFailure {
                _state.value = SavedMoviesReducer.error(
                    state = _state.value,
                    message = "Film nije uklonjen na serveru.",
                )
            }
        }
    }
}
