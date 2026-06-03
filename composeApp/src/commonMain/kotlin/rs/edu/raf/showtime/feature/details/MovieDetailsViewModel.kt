package rs.edu.raf.showtime.feature.details

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieRepository

class MovieDetailsViewModel(
    private val movieId: String,
    private val repository: MovieRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(MovieDetailsState())
    val state: StateFlow<MovieDetailsState> = _state.asStateFlow()

    init {
        observeMovie()
        refreshMovie()
    }

    fun onIntent(intent: MovieDetailsIntent) {
        when (intent) {
            MovieDetailsIntent.Refresh -> refreshMovie()
            is MovieDetailsIntent.FavoriteChanged -> scope.launch {
                try {
                    repository.setFavorite(intent.movieId, intent.value)
                } catch (_: Exception) {
                    _state.value = _state.value.copy(error = "Favorite nije sačuvan na serveru.")
                }
            }
            is MovieDetailsIntent.WatchlistChanged -> scope.launch {
                try {
                    repository.setWatchlisted(intent.movieId, intent.value)
                } catch (_: Exception) {
                    _state.value = _state.value.copy(error = "Watchlist nije sačuvan na serveru.")
                }
            }
        }
    }

    private fun observeMovie() {
        scope.launch {
            repository.observeMovie(movieId).collect { movie ->
                _state.value = _state.value.copy(movie = movie)
            }
        }
    }

    private fun refreshMovie() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                repository.refreshMovieDetails(movieId)
                _state.value = _state.value.copy(isLoading = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Detalji filma nisu osveženi.",
                )
            }
        }
    }
}
