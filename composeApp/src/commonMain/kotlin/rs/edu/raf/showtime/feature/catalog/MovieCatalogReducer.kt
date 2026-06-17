package rs.edu.raf.showtime.feature.catalog

import rs.edu.raf.showtime.data.movie.MovieCatalogPage

object MovieCatalogReducer {

    fun reduce(state: MovieCatalogState, intent: MovieCatalogIntent): MovieCatalogState {
        return when (intent) {
            MovieCatalogIntent.Refresh,
            MovieCatalogIntent.BackClicked,
            is MovieCatalogIntent.MovieClicked,
            is MovieCatalogIntent.FavoriteChanged,
            is MovieCatalogIntent.WatchlistChanged -> state

            MovieCatalogIntent.NextPage -> {
                if (state.canGoNext) state.copy(page = state.page + 1, error = null) else state
            }

            MovieCatalogIntent.PreviousPage -> state.copy(
                page = maxOf(1, state.page - 1),
                error = null,
            )

            is MovieCatalogIntent.SearchChanged -> state.copy(query = intent.value, page = 1, error = null)
            is MovieCatalogIntent.GenreChanged -> state.copy(genre = intent.value, page = 1, error = null)
            is MovieCatalogIntent.MinYearChanged -> state.copy(
                minYear = intent.value.filter(Char::isDigit).take(4),
                page = 1,
                error = null,
            )
            is MovieCatalogIntent.MaxYearChanged -> state.copy(
                maxYear = intent.value.filter(Char::isDigit).take(4),
                page = 1,
                error = null,
            )
            is MovieCatalogIntent.MinRatingChanged -> state.copy(
                minRating = normalizeRating(intent.value),
                page = 1,
                error = null,
            )
            is MovieCatalogIntent.SortChanged -> state.copy(sortBy = intent.sortBy, page = 1, error = null)
            is MovieCatalogIntent.SortOrderChanged -> state.copy(sortOrder = intent.value, page = 1, error = null)
        }
    }

    fun pageLoaded(state: MovieCatalogState, page: MovieCatalogPage): MovieCatalogState {
        return state.copy(
            movies = page.movies,
            totalItems = page.totalItems,
            totalPages = page.totalPages,
        )
    }

    fun genresLoaded(state: MovieCatalogState, genres: List<GenreFilter>): MovieCatalogState {
        return state.copy(
            genres = listOf(GenreFilter(null, "Svi")) + genres,
            genreError = null,
        )
    }

    fun genresError(state: MovieCatalogState, message: String): MovieCatalogState {
        return state.copy(genreError = message)
    }

    fun loading(state: MovieCatalogState): MovieCatalogState {
        return state.copy(isLoading = true, error = null)
    }

    fun idle(state: MovieCatalogState): MovieCatalogState {
        return state.copy(isLoading = false, isOffline = false, error = null)
    }

    fun offline(state: MovieCatalogState, message: String): MovieCatalogState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }

    fun error(state: MovieCatalogState, message: String): MovieCatalogState {
        return state.copy(isLoading = false, error = message)
    }

    private fun normalizeRating(value: String): String {
        val normalized = value.replace(',', '.')
        if (normalized.isBlank()) return ""
        if (!Regex("""^\d{0,2}(\.\d{0,2})?$""").matches(normalized)) return value.dropLast(1)
        if ((normalized.toDoubleOrNull() ?: 0.0) > 10.0) return "10"
        return normalized
    }
}
