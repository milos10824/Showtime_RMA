package rs.edu.raf.showtime.feature.details

import rs.edu.raf.showtime.domain.movie.MovieDetails

object MovieDetailsReducer {

    fun movieLoaded(state: MovieDetailsState, movie: MovieDetails?): MovieDetailsState {
        return state.copy(movie = movie)
    }

    fun loading(state: MovieDetailsState): MovieDetailsState {
        return state.copy(isLoading = true, isOffline = false, error = null)
    }

    fun idle(state: MovieDetailsState): MovieDetailsState {
        return state.copy(isLoading = false, isOffline = false, error = null)
    }

    fun error(state: MovieDetailsState, message: String): MovieDetailsState {
        return state.copy(isLoading = false, error = message)
    }

    fun offline(state: MovieDetailsState, message: String): MovieDetailsState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }
}
