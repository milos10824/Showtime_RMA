package rs.edu.raf.showtime.networking

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
    private val publicClient: HttpClient,
    private val authenticatedClient: HttpClient,
) {
    private val baseUrl = "https://rma.finlab.rs/"

    suspend fun login(username: String, password: String): AuthResponseApiModel {
        return publicClient.post(baseUrl + "auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestApiModel(username = username, password = password))
        }.body()
    }

    suspend fun signup(fullName: String, username: String, password: String): AuthResponseApiModel {
        return publicClient.post(baseUrl + "auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(SignupRequestApiModel(fullName = fullName, username = username, password = password))
        }.body()
    }

    suspend fun getProfile(): UserApiModel {
        return authenticatedClient.get(baseUrl + "me").body()
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
        return publicClient.get(baseUrl + "movies") {
            parameter("page", page)
            parameter("page_size", pageSize)
            query?.takeIf { it.isNotBlank() }?.let { parameter("query", it) }
            genreId?.let { parameter("genre_id", it) }
            minYear?.let { parameter("min_year", it) }
            maxYear?.let { parameter("max_year", it) }
            minRating?.let { parameter("min_rating", it) }
            sortBy?.takeIf { it.isNotBlank() }?.let {
                parameter("sort_by", it)
                parameter("sort_order", sortOrder)
            }
        }.body()
    }

    suspend fun getMovie(id: String): MovieApiModel {
        return publicClient.get(baseUrl + "movies/$id").body()
    }

    suspend fun getMovieCast(
        id: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): PaginatedResponse<PersonSummaryApiModel> {
        return publicClient.get(baseUrl + "movies/$id/cast") {
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body()
    }

    suspend fun getMovieImages(id: String, type: String? = null): MovieImagesApiModel {
        return publicClient.get(baseUrl + "movies/$id/images") {
            type?.takeIf { it.isNotBlank() }?.let { parameter("type", it) }
        }.body()
    }

    suspend fun getGenres(): List<GenreApiModel> {
        return publicClient.get(baseUrl + "genres").body()
    }

    suspend fun getConfig(): List<ConfigEntryApiModel> {
        return publicClient.get(baseUrl + "config").body()
    }

    suspend fun getFavorites(): List<MovieListItemApiModel> {
        return authenticatedClient.get(baseUrl + "me/favorites").body()
    }

    suspend fun addFavorite(movieId: String) {
        authenticatedClient.post(baseUrl + "me/favorites/$movieId")
    }

    suspend fun removeFavorite(movieId: String) {
        authenticatedClient.delete(baseUrl + "me/favorites/$movieId")
    }

    suspend fun getWatchlist(): List<MovieListItemApiModel> {
        return authenticatedClient.get(baseUrl + "me/watchlist").body()
    }

    suspend fun addWatchlist(movieId: String) {
        authenticatedClient.post(baseUrl + "me/watchlist/$movieId")
    }

    suspend fun removeWatchlist(movieId: String) {
        authenticatedClient.delete(baseUrl + "me/watchlist/$movieId")
    }

    suspend fun submitQuizResult(score: Double) {
        authenticatedClient.post(baseUrl + "leaderboard") {
            contentType(ContentType.Application.Json)
            setBody(QuizResultRequestApiModel(score = score))
        }
    }
}
