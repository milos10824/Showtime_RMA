package rs.edu.raf.showtime.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.movie.MovieCatalogQuery
import rs.edu.raf.showtime.data.movie.MovieRepository

class MovieCatalogViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieCatalogState())
    val state: StateFlow<MovieCatalogState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var refreshJob: Job? = null
    private val events = MutableSharedFlow<MovieCatalogIntent>()
    private val _effect = MutableSharedFlow<MovieCatalogEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeEvents()
        observeGenres()
        refreshGenres()
        restartCatalog()
        syncUserLists()
    }

    fun onIntent(intent: MovieCatalogIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: MovieCatalogIntent) {
        when (intent) {
            MovieCatalogIntent.Refresh -> {
                refreshCatalog()
                syncUserLists()
            }

            MovieCatalogIntent.NextPage,
            MovieCatalogIntent.PreviousPage,
            is MovieCatalogIntent.GenreChanged,
            is MovieCatalogIntent.SortChanged,
            is MovieCatalogIntent.SortOrderChanged -> {
                _state.value = MovieCatalogReducer.reduce(_state.value, intent)
                restartCatalog()
            }

            is MovieCatalogIntent.SearchChanged,
            is MovieCatalogIntent.MinYearChanged,
            is MovieCatalogIntent.MaxYearChanged,
            is MovieCatalogIntent.MinRatingChanged -> {
                _state.value = MovieCatalogReducer.reduce(_state.value, intent)
                restartCatalog(debounce = true)
            }

            is MovieCatalogIntent.FavoriteChanged -> updateFavorite(intent)
            is MovieCatalogIntent.WatchlistChanged -> updateWatchlist(intent)
            is MovieCatalogIntent.MovieClicked -> viewModelScope.launch {
                _effect.emit(MovieCatalogEffect.OpenMovieDetails(intent.movieId))
            }
            MovieCatalogIntent.BackClicked -> viewModelScope.launch {
                _effect.emit(MovieCatalogEffect.Close)
            }
        }
    }

    private fun restartCatalog(debounce: Boolean = false) {
        observeCatalog()
        refreshCatalog(debounce)
    }

    private fun observeCatalog() {
        observeJob?.cancel()
        val query = _state.value.toQuery()
        observeJob = viewModelScope.launch {
            repository.observeCatalog(query).collect { page ->
                _state.value = MovieCatalogReducer.pageLoaded(_state.value, page)
            }
        }
    }

    private fun refreshCatalog(debounce: Boolean = false) {
        refreshJob?.cancel()
        val query = _state.value.toQuery()
        refreshJob = viewModelScope.launch {
            if (debounce) delay(350)
            validateFilters(_state.value)?.let { message ->
                _state.value = MovieCatalogReducer.error(_state.value, message)
                return@launch
            }
            _state.value = MovieCatalogReducer.loading(_state.value)
            runCatching { repository.refreshCatalog(query) }
                .onSuccess { _state.value = MovieCatalogReducer.idle(_state.value) }
                .onFailure { error ->
                    _state.value = if (error is ClientRequestException) {
                        MovieCatalogReducer.error(
                            state = _state.value,
                            message = "Server nije prihvatio izabrane filtere.",
                        )
                    } else {
                        MovieCatalogReducer.offline(
                            state = _state.value,
                            message = "Nema mreže. Prikazujem sačuvanu stranicu.",
                        )
                    }
                }
        }
    }

    private fun observeGenres() {
        viewModelScope.launch {
            repository.observeGenres().collect { genres ->
                _state.value = MovieCatalogReducer.genresLoaded(
                    state = _state.value,
                    genres = genres.map { GenreFilter(id = it.id, name = it.name) },
                )
            }
        }
    }

    private fun refreshGenres() {
        viewModelScope.launch {
            runCatching { repository.refreshGenres() }
                .onFailure { error ->
                    _state.value = MovieCatalogReducer.genresError(
                        state = _state.value,
                        message = if (error is ClientRequestException) {
                            "Server nije osvežio žanrove."
                        } else {
                            "Žanrovi nisu osveženi. Prikazujem lokalno sačuvane opcije."
                        },
                    )
                }
        }
    }

    private fun syncUserLists() {
        viewModelScope.launch {
            runCatching {
                repository.syncFavorites()
                repository.syncWatchlist()
            }
        }
    }

    private fun updateFavorite(intent: MovieCatalogIntent.FavoriteChanged) {
        viewModelScope.launch {
            runCatching { repository.setFavorite(intent.movieId, intent.value) }
                .onFailure {
                    _state.value = MovieCatalogReducer.error(
                        state = _state.value,
                        message = "Favorite nije sačuvan na serveru.",
                    )
                }
        }
    }

    private fun updateWatchlist(intent: MovieCatalogIntent.WatchlistChanged) {
        viewModelScope.launch {
            runCatching { repository.setWatchlisted(intent.movieId, intent.value) }
                .onFailure {
                    _state.value = MovieCatalogReducer.error(
                        state = _state.value,
                        message = "Watchlist nije sačuvan na serveru.",
                    )
                }
        }
    }

    private fun MovieCatalogState.toQuery(): MovieCatalogQuery {
        return MovieCatalogQuery(
            page = page,
            pageSize = pageSize,
            query = query.trim().ifBlank { null },
            genreId = genre.id,
            minYear = minYear.toIntOrNull(),
            maxYear = maxYear.toIntOrNull(),
            minRating = minRating.toDoubleOrNull()?.takeIf { it in 0.0..10.0 },
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
    }

    private fun validateFilters(state: MovieCatalogState): String? {
        val minYear = state.minYear.toIntOrNull()
        val maxYear = state.maxYear.toIntOrNull()
        if (minYear != null && maxYear != null && minYear > maxYear) {
            return "Početna godina ne može biti veća od krajnje."
        }
        return null
    }
}
