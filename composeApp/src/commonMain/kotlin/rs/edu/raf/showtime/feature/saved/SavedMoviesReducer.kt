package rs.edu.raf.showtime.feature.saved

import rs.edu.raf.showtime.domain.movie.MovieListItem

object SavedMoviesReducer {

    fun moviesLoaded(state: SavedMoviesState, movies: List<MovieListItem>): SavedMoviesState {
        return state.copy(movies = movies)
    }

    fun loading(state: SavedMoviesState): SavedMoviesState {
        return state.copy(isLoading = true, isOffline = false, error = null)
    }

    fun idle(state: SavedMoviesState): SavedMoviesState {
        return state.copy(isLoading = false, isOffline = false, error = null)
    }

    fun error(state: SavedMoviesState, message: String): SavedMoviesState {
        return state.copy(isLoading = false, error = message)
    }

    fun offline(state: SavedMoviesState, message: String): SavedMoviesState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }
}
