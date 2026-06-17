package rs.edu.raf.showtime.networking.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.edu.raf.showtime.core.auth.AuthStore
import rs.edu.raf.showtime.core.auth.SessionManager
import rs.edu.raf.showtime.networking.ShowtimeApi

private val publicClient = named("publicHttpClient")
private val authenticatedClient = named("authenticatedHttpClient")

val networkingModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    single(publicClient) {
        createHttpClient(json = get())
    }

    single(authenticatedClient) {
        val authStore = get<AuthStore>()
        val sessionManager = get<SessionManager>()
        createHttpClient(json = get()) {
            installShowtimeAuth(authStore, sessionManager)
        }
    }

    single {
        ShowtimeApi(
            publicClient = get(publicClient),
            authenticatedClient = get(authenticatedClient),
        )
    }
}

private fun createHttpClient(
    json: Json,
    configure: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
    return HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json = json)
        }

        install(Logging) {
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
        }

        configure()
    }
}

private fun HttpClientConfig<*>.installShowtimeAuth(
    authStore: AuthStore,
    sessionManager: SessionManager,
) {
    install(createClientPlugin("ShowtimeAuth") {
        on(SetupRequest) { request ->
            authStore.authData.first().token?.let { token ->
                request.header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

    })

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val response = (cause as? ClientRequestException)?.response
            if (response?.status == HttpStatusCode.Unauthorized) {
                sessionManager.clearSession()
            }
        }
    }
}
