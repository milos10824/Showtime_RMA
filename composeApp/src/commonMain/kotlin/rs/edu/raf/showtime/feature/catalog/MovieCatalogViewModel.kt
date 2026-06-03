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
        syncUserLists()
    }

    fun onIntent(intent: MovieCatalogIntent) {
        when (intent) {
            MovieCatalogIntent.Refresh -> {
                refreshMovies()
                syncUserLists()
            }

            MovieCatalogIntent.NextPage -> {
                val nextPage = _state.value.page + 1
                _state.value = _state.value.copy(page = nextPage)
                refreshMovies()
            }

            MovieCatalogIntent.PreviousPage -> {
                val previousPage = maxOf(1, _state.value.page - 1)
                _state.value = _state.value.copy(page = previousPage)
                refreshMovies()
            }

            is MovieCatalogIntent.SearchChanged -> {
                _state.value = _state.value.copy(
                    query = intent.value,
                    page = 1,
                    error = null,
                )

                observeMovies(intent.value)
                refreshMovies()
            }

            is MovieCatalogIntent.GenreChanged -> {
                _state.value = _state.value.copy(
                    genre = intent.value,
                    page = 1,
                    error = null,
                )
                refreshMovies()
            }

            is MovieCatalogIntent.MinYearChanged -> {
                _state.value = _state.value.copy(
                    minYear = intent.value,
                    page = 1,
                    error = null,
                )
                refreshMovies()
            }

            is MovieCatalogIntent.MaxYearChanged -> {
                _state.value = _state.value.copy(
                    maxYear = intent.value,
                    page = 1,
                    error = null,
                )
                refreshMovies()
            }

            is MovieCatalogIntent.MinRatingChanged -> {
                _state.value = _state.value.copy(
                    minRating = intent.value,
                    page = 1,
                    error = null,
                )
                refreshMovies()
            }

            is MovieCatalogIntent.SortChanged -> {
                _state.value = _state.value.copy(
                    sortBy = intent.sortBy,
                    page = 1,
                    error = null,
                )
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
            val currentState = _state.value

            _state.value = currentState.copy(
                isLoading = true,
                error = null,
            )

            try {
                repository.refreshMovies(
                    page = currentState.page,
                    pageSize = 20,
                    query = currentState.query.trim().ifBlank { null },
                    genreId = currentState.genre.id,
                    minYear = currentState.minYear.toIntOrNull(),
                    maxYear = currentState.maxYear.toIntOrNull(),
                    minRating = currentState.minRating.toDoubleOrNull(),
                    sortBy = currentState.sortBy,
                    sortOrder = currentState.sortOrder,
                )

                repository.restoreCurrentUserMovieData()

                _state.value = _state.value.copy(isLoading = false)
            } catch (_: Exception) {
                repository.restoreCurrentUserMovieData()

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Filmovi nisu osveženi. Prikazujem lokalne podatke.",
                )
            }
        }
    }

    private fun syncUserLists() {
        scope.launch {
            repository.restoreCurrentUserMovieData()
            repository.syncFavorites()
            repository.syncWatchlist()
        }
    }
}
