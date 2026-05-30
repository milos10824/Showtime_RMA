package rs.edu.raf.showtime.feature.catalog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieRepository

class MovieCatalogViewModel(
    private val repository: MovieRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(MovieCatalogState())
    val state: StateFlow<MovieCatalogState> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeMovies("")
        refreshMovies()
    }

    fun onIntent(intent: MovieCatalogIntent) {
        when (intent) {
            MovieCatalogIntent.Refresh -> refreshMovies()

            is MovieCatalogIntent.SearchChanged -> {
                _state.value = _state.value.copy(
                    query = intent.value,
                    error = null,
                )
                observeMovies(intent.value)
                refreshMovies()
            }

            is MovieCatalogIntent.FavoriteChanged -> {
                scope.launch {
                    repository.setFavorite(
                        movieId = intent.movieId,
                        isFavorite = intent.value,
                    )
                }
            }

            is MovieCatalogIntent.WatchlistChanged -> {
                scope.launch {
                    repository.setWatchlisted(
                        movieId = intent.movieId,
                        isWatchlisted = intent.value,
                    )
                }
            }
        }
    }

    private fun observeMovies(query: String) {
        observeJob?.cancel()

        observeJob = scope.launch {
            repository.searchMovies(query).collect { movies ->
                _state.value = _state.value.copy(movies = movies)
            }
        }
    }

    private fun refreshMovies() {
        scope.launch {
            val currentQuery = _state.value.query.trim()

            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
            )

            try {
                repository.refreshMovies(
                    query = currentQuery.ifBlank { null },
                )

                _state.value = _state.value.copy(isLoading = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Filmovi nisu osveženi. Proveri internet konekciju.",
                )
            }
        }
    }
}
