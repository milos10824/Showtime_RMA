package rs.edu.raf.showtime.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle

@Composable
fun AuthScreen(
    state: AuthState,
    onIntent: (AuthIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreen{
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTitle(text = if (state.isSignup) "Create account" else "Welcome to Showtime")
            if (state.isSignup) {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = { onIntent(AuthIntent.FullNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = state.username,
                onValueChange = { onIntent(AuthIntent.UsernameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
            )

            state.error?.let { message ->
                Text(text = message, modifier = Modifier.padding(top = 4.dp))
            }

            Button(
                onClick = { onIntent(AuthIntent.Submit) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.isLoading) {
                        "Please wait..."
                    } else if (state.isSignup) {
                        "Sign up"
                    } else {
                        "Login"
                    }
                )
            }

            OutlinedButton(
                onClick = { onIntent(AuthIntent.ChangeMode) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.isSignup) {
                        "Already have an account? Login"
                    } else {
                        "No account? Sign up"
                    }
                )
            }
        }
    }
}
