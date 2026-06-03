package rs.edu.raf.showtime.networking

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import rs.edu.raf.showtime.networking.model.AuthResponseApiModel
import rs.edu.raf.showtime.networking.model.ConfigEntryApiModel
import rs.edu.raf.showtime.networking.model.GenreApiModel
import rs.edu.raf.showtime.networking.model.LoginRequestApiModel
import rs.edu.raf.showtime.networking.model.MovieApiModel
import rs.edu.raf.showtime.networking.model.MovieImagesApiModel
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel
import rs.edu.raf.showtime.networking.model.PaginatedResponse
import rs.edu.raf.showtime.networking.model.PersonSummaryApiModel
import rs.edu.raf.showtime.networking.model.QuizResultRequestApiModel
import rs.edu.raf.showtime.networking.model.SignupRequestApiModel
import rs.edu.raf.showtime.networking.model.UserApiModel

class ShowtimeApi(
    private val client: HttpClient,
) {
    private val baseUrl = "https://rma.finlab.rs/"

    suspend fun login(username: String, password: String): AuthResponseApiModel {
        return client.post(baseUrl + "auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestApiModel(username = username, password = password))
        }.body()
    }

    suspend fun signup(fullName: String, username: String, password: String): AuthResponseApiModel {
        return client.post(baseUrl + "auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(SignupRequestApiModel(fullName = fullName, username = username, password = password))
        }.body()
    }

    suspend fun getProfile(token: String): UserApiModel {
        return client.get(baseUrl + "me") {
            bearerAuth(token)
        }.body()
    }

    suspend fun getMovies(
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Double? = null,
        sortBy: String? = null,
        sortOrder: String = "desc",
    ): PaginatedResponse<MovieListItemApiModel> {
        return client.get(baseUrl + "movies") {
            parameter("page", page)
            parameter("page_size", pageSize)

            if (!query.isNullOrBlank()) {
                parameter("query", query)
            }
            if (genreId != null) {
                parameter("genre_id", genreId)
            }
            if (minYear != null) {
                parameter("min_year", minYear)
            }
            if (maxYear != null) {
                parameter("max_year", maxYear)
            }
            if (minRating != null) {
                parameter("min_rating", minRating)
            }
            if (!sortBy.isNullOrBlank()) {
                parameter("sort_by", sortBy)
                parameter("sort_order", sortOrder)
            }
        }.body()
    }

    suspend fun getMovie(id: String): MovieApiModel {
        return client.get(baseUrl + "movies/$id").body()
    }

    suspend fun getMovieCast(id: String, page: Int = 1, pageSize: Int = 20): PaginatedResponse<PersonSummaryApiModel> {
        return client.get(baseUrl + "movies/$id/cast") {
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body()
    }

    suspend fun getMovieImages(id: String, type: String? = null): MovieImagesApiModel {
        return client.get(baseUrl + "movies/$id/images") {
            if (!type.isNullOrBlank()) {
                parameter("type", type)
            }
        }.body()
    }

    suspend fun getGenres(): List<GenreApiModel> {
        return client.get(baseUrl + "genres").body()
    }

    suspend fun getConfig(): List<ConfigEntryApiModel> {
        return client.get(baseUrl + "config").body()
    }

    suspend fun getFavorites(token: String): List<MovieListItemApiModel> {
        return client.get(baseUrl + "me/favorites") {
            bearerAuth(token)
        }.body()
    }

    suspend fun addFavorite(token: String, movieId: String) {
        client.post(baseUrl + "me/favorites/$movieId") {
            bearerAuth(token)
        }
    }

    suspend fun removeFavorite(token: String, movieId: String) {
        client.delete(baseUrl + "me/favorites/$movieId") {
            bearerAuth(token)
        }
    }

    suspend fun getWatchlist(token: String): List<MovieListItemApiModel> {
        return client.get(baseUrl + "me/watchlist") {
            bearerAuth(token)
        }.body()
    }

    suspend fun addWatchlist(token: String, movieId: String) {
        client.post(baseUrl + "me/watchlist/$movieId") {
            bearerAuth(token)
        }
    }

    suspend fun removeWatchlist(token: String, movieId: String) {
        client.delete(baseUrl + "me/watchlist/$movieId") {
            bearerAuth(token)
        }
    }

    suspend fun submitQuizResult(token: String, score: Double) {
        client.post(baseUrl + "leaderboard") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(QuizResultRequestApiModel(score = score))
        }
    }
}
