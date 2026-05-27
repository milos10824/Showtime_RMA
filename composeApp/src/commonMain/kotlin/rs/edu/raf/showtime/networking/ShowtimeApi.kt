package rs.edu.raf.showtime.networking

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import rs.edu.raf.showtime.networking.model.ConfigEntryApiModel
import rs.edu.raf.showtime.networking.model.GenreApiModel
import rs.edu.raf.showtime.networking.model.MovieApiModel
import rs.edu.raf.showtime.networking.model.MovieImagesApiModel
import rs.edu.raf.showtime.networking.model.MovieListItemApiModel
import rs.edu.raf.showtime.networking.model.PaginatedResponse
import rs.edu.raf.showtime.networking.model.PersonSummaryApiModel

class ShowtimeApi(
    private val client: HttpClient,
) {
    private val baseUrl = "https://rma.finlab.rs/"

    suspend fun getMovies(
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Double? = null,
        sortBy: String = "imdb_votes",
        sortOrder: String = "desc",
    ): PaginatedResponse<MovieListItemApiModel> {
        return client.get(baseUrl + "movies") {
            parameter("page", page)
            parameter("page_size", pageSize)
            parameter("query", query)
            parameter("genre_id", genreId)
            parameter("min_year", minYear)
            parameter("max_year", maxYear)
            parameter("min_rating", minRating)
            parameter("sort_by", sortBy)
            parameter("sort_order", sortOrder)
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
            parameter("type", type)
        }.body()
    }

    suspend fun getGenres(): List<GenreApiModel> {
        return client.get(baseUrl + "genres").body()
    }

    suspend fun getConfig(): List<ConfigEntryApiModel> {
        return client.get(baseUrl + "config").body()
    }
}
