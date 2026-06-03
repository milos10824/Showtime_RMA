package rs.edu.raf.showtime.feature.catalog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieRepository
import rs.edu.raf.showtime.domain.movie.MovieListItem

class MovieCatalogViewModel(
    private val repository: MovieRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(MovieCatalogState())
    val state: StateFlow<MovieCatalogState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var cachedMovies: List<MovieListItem> = emptyList()

    init {
        observeMovies("")
        refreshMovies()
        scope.launch {
            try {
                repository.syncFavorites()
                repository.syncWatchlist()
            } catch (_: Exception) {
                // Katalog može da radi i ako se korisničke liste ne sinhronizuju odmah.
            }
        }
    }

    fun onIntent(intent: MovieCatalogIntent) {
        when (intent) {
            MovieCatalogIntent.Refresh -> refreshMovies()

            MovieCatalogIntent.NextPage -> {
                _state.value = _state.value.copy(page = _state.value.page + 1)
                refreshMovies()
            }

            MovieCatalogIntent.PreviousPage -> {
                val newPage = maxOf(1, _state.value.page - 1)
                _state.value = _state.value.copy(page = newPage)
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

            is MovieCatalogIntent.GenreChanged -> updateFilter {
                copy(genre = intent.value, page = 1)
            }

            is MovieCatalogIntent.MinYearChanged -> updateFilter {
                copy(minYear = intent.value, page = 1)
            }

            is MovieCatalogIntent.MaxYearChanged -> updateFilter {
                copy(maxYear = intent.value, page = 1)
            }

            is MovieCatalogIntent.MinRatingChanged -> updateFilter {
                copy(minRating = intent.value, page = 1)
            }

            is MovieCatalogIntent.SortChanged -> {
                val order = if (intent.sortBy == "title") "asc" else "desc"
                updateFilter {
                    copy(
                        sortBy = intent.sortBy,
                        sortOrder = order,
                        page = 1,
                    )
                }
            }

            is MovieCatalogIntent.FavoriteChanged -> {
                scope.launch {
                    try {
                        repository.setFavorite(intent.movieId, intent.value)
                    } catch (_: Exception) {
                        _state.value = _state.value.copy(
                            error = "Favorite promena nije sačuvana na serveru.",
                        )
                    }
                }
            }

            is MovieCatalogIntent.WatchlistChanged -> {
                scope.launch {
                    try {
                        repository.setWatchlisted(intent.movieId, intent.value)
                    } catch (_: Exception) {
                        _state.value = _state.value.copy(
                            error = "Watchlist promena nije sačuvana na serveru.",
                        )
                    }
                }
            }
        }
    }

    private fun updateFilter(block: MovieCatalogState.() -> MovieCatalogState) {
        val nextState = block(_state.value).copy(error = null)
        _state.value = nextState.copy(movies = filterMovies(cachedMovies, nextState))
        refreshMovies()
    }

    private fun observeMovies(query: String) {
        observeJob?.cancel()

        observeJob = scope.launch {
            repository.searchMovies(query).collect { movies ->
                cachedMovies = movies
                _state.value = _state.value.copy(
                    movies = filterMovies(movies, _state.value),
                )
            }
        }
    }

    private fun filterMovies(
        movies: List<MovieListItem>,
        state: MovieCatalogState,
    ): List<MovieListItem> {
        val minYear = state.minYear.toIntOrNull()
        val maxYear = state.maxYear.toIntOrNull()
        val minRating = state.minRating.toDoubleOrNull()

        val filtered = movies.filter { movie ->
            val yearOk = minYear == null || (movie.year != null && movie.year >= minYear)
            val maxYearOk = maxYear == null || (movie.year != null && movie.year <= maxYear)
            val ratingOk = minRating == null || (movie.imdbRating != null && movie.imdbRating >= minRating)
            val genreOk = state.genre.id == null || movie.genres.contains(state.genre.name)

            yearOk && maxYearOk && ratingOk && genreOk
        }

        return when (state.sortBy) {
            "title" -> filtered.sortedBy { it.title }
            "year" -> filtered.sortedByDescending { it.year ?: 0 }
            "imdb_rating" -> filtered.sortedByDescending { it.imdbRating ?: 0.0 }
            else -> filtered.sortedByDescending { it.imdbVotes ?: 0 }
        }
    }

    private fun refreshMovies() {
        scope.launch {
            val current = _state.value

            _state.value = current.copy(
                isLoading = true,
                error = null,
            )

            try {
                repository.refreshMovies(
                    page = current.page,
                    pageSize = 20,
                    query = current.query.trim().ifBlank { null },
                    genreId = current.genre.id,
                    minYear = current.minYear.toIntOrNull(),
                    maxYear = current.maxYear.toIntOrNull(),
                    minRating = current.minRating.toDoubleOrNull(),
                    sortBy = current.sortBy,
                    sortOrder = current.sortOrder,
                )

                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                println("Filmovi nisu osveženi: ${e::class.simpleName} - ${e.message}")

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Filmovi nisu osveženi. Prikazujem lokalne podatke.",
                )
            }
        }
    }
}
