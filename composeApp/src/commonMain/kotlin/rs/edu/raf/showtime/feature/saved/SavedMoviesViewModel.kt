package rs.edu.raf.showtime.feature.saved

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieRepository

class SavedMoviesViewModel(
    private val type: SavedListType,
    private val repository: MovieRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(SavedMoviesState(type = type))
    val state: StateFlow<SavedMoviesState> = _state.asStateFlow()

    init {
        observeMovies()
        refresh()
    }

    fun onIntent(intent: SavedMoviesIntent) {
        when (intent) {
            SavedMoviesIntent.Refresh -> refresh()
            is SavedMoviesIntent.RemoveClicked -> removeMovie(intent.movieId)
        }
    }

    private fun observeMovies() {
        scope.launch {
            val flow = when (type) {
                SavedListType.FAVORITES -> repository.observeFavorites()
                SavedListType.WATCHLIST -> repository.observeWatchlist()
            }

            flow.collect { movies ->
                _state.value = _state.value.copy(movies = movies)
            }
        }
    }

    private fun refresh() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                when (type) {
                    SavedListType.FAVORITES -> repository.syncFavorites()
                    SavedListType.WATCHLIST -> repository.syncWatchlist()
                }
                _state.value = _state.value.copy(isLoading = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Lista nije osvežena. Prikazani su lokalni podaci.",
                )
            }
        }
    }

    private fun removeMovie(movieId: String) {
        scope.launch {
            try {
                when (type) {
                    SavedListType.FAVORITES -> repository.setFavorite(movieId, false)
                    SavedListType.WATCHLIST -> repository.setWatchlisted(movieId, false)
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Film nije uklonjen sa servera.")
            }
        }
    }
}
