package rs.edu.raf.showtime.feature.details

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
import rs.edu.raf.showtime.navigation.movieIdOrThrow

class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {
    private val movieId = savedStateHandle.movieIdOrThrow

    private val _state = MutableStateFlow(MovieDetailsState())
    val state: StateFlow<MovieDetailsState> = _state.asStateFlow()
    private val events = MutableSharedFlow<MovieDetailsIntent>()
    private val _effect = MutableSharedFlow<MovieDetailsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeEvents()
        observeMovie()
        refreshMovie()
    }

    fun onIntent(intent: MovieDetailsIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: MovieDetailsIntent) {
        when (intent) {
            MovieDetailsIntent.Refresh -> refreshMovie()
            MovieDetailsIntent.BackClicked -> viewModelScope.launch {
                _effect.emit(MovieDetailsEffect.Close)
            }
            is MovieDetailsIntent.FavoriteChanged -> viewModelScope.launch {
                try {
                    repository.setFavorite(intent.movieId, intent.value)
                } catch (_: Exception) {
                    _state.value = MovieDetailsReducer.error(
                        state = _state.value,
                        message = "Favorite nije sačuvan na serveru.",
                    )
                }
            }
            is MovieDetailsIntent.WatchlistChanged -> viewModelScope.launch {
                try {
                    repository.setWatchlisted(intent.movieId, intent.value)
                } catch (_: Exception) {
                    _state.value = MovieDetailsReducer.error(
                        state = _state.value,
                        message = "Watchlist nije sačuvan na serveru.",
                    )
                }
            }
        }
    }

    private fun observeMovie() {
        viewModelScope.launch {
            repository.observeMovie(movieId).collect { movie ->
                _state.value = MovieDetailsReducer.movieLoaded(_state.value, movie)
            }
        }
    }

    private fun refreshMovie() {
        viewModelScope.launch {
            _state.value = MovieDetailsReducer.loading(_state.value)
            try {
                repository.refreshMovieDetails(movieId)
                _state.value = MovieDetailsReducer.idle(_state.value)
            } catch (error: Exception) {
                _state.value = if (error is ClientRequestException) {
                    MovieDetailsReducer.error(_state.value, "Detalji filma nisu pronađeni na serveru.")
                } else {
                    MovieDetailsReducer.offline(_state.value, "Nema mreže. Prikazujem sačuvane detalje.")
                }
            }
        }
    }
}
