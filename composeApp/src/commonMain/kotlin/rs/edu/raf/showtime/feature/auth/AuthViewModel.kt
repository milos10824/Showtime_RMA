package rs.edu.raf.showtime.feature.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.auth.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AuthIntent) {
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

        scope.launch {
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
                _effect.send(AuthEffect.OpenHome)
            } catch (_: Exception) {
                _state.value = AuthReducer.error(
                    state = _state.value,
                    message = "Prijava nije uspela. Proveri podatke i internet konekciju.",
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
}
