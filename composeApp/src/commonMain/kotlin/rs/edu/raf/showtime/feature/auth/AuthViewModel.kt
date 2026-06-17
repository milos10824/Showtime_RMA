package rs.edu.raf.showtime.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.auth.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val events = MutableSharedFlow<AuthIntent>()
    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AuthIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    init {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: AuthIntent) {
        if (intent == AuthIntent.Submit) {
            submit()
            return
        }

        _state.value = AuthReducer.reduce(state = _state.value, intent = intent)
    }

    private fun submit() {
        val currentState = _state.value
        val validationError = validate(currentState)

        if (validationError != null) {
            _state.value = AuthReducer.error(state = currentState, message = validationError)
            return
        }

        viewModelScope.launch {
            _state.value = AuthReducer.loading(currentState)

            try {
                if (currentState.isSignup) {
                    repository.signup(
                        fullName = currentState.fullName.trim(),
                        username = currentState.username.trim(),
                        password = currentState.password,
                    )
                } else {
                    repository.login(
                        username = currentState.username.trim(),
                        password = currentState.password,
                    )
                }

                _state.value = AuthReducer.idle(_state.value)
                _effect.emit(AuthEffect.OpenHome)
            } catch (e: Exception) {
                _state.value = AuthReducer.error(
                    state = _state.value,
                    message = authErrorMessage(e, currentState.isSignup),
                    isOffline = e !is ClientRequestException,
                )
            }
        }
    }

    private fun validate(state: AuthState): String? {
        val usernameRegex = Regex("^[A-Za-z0-9_]{3,}$")

        if (state.isSignup && state.fullName.isBlank()) {
            return "Unesi puno ime."
        }

        if (!usernameRegex.matches(state.username.trim())) {
            return "Username mora imati bar 3 karaktera i sme sadržati slova, cifre i donju crtu."
        }

        if (state.password.length < 8) {
            return "Password mora imati najmanje 8 karaktera."
        }

        return null
    }

    private fun authErrorMessage(error: Throwable, isSignup: Boolean): String {
        if (error is ClientRequestException) {
            return when (error.response.status) {
                HttpStatusCode.Conflict -> "Username je zauzet."
                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden -> "Nevalidno korisničko ime ili lozinka."
                HttpStatusCode.BadRequest -> if (isSignup) {
                    "Registracija nije validna. Proveri puno ime, username i lozinku."
                } else {
                    "Prijava nije validna. Proveri username i lozinku."
                }
                else -> "Server trenutno nije prihvatio zahtev. Pokušaj ponovo."
            }
        }

        return "Mrežna greška. Proveri internet konekciju i pokušaj ponovo."
    }
}
