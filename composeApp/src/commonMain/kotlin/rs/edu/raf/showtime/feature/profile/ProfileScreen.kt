package rs.edu.raf.showtime.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = "Profil")
            AppInfoText(text = "Korisničko ime: ${state.authData.username ?: "-"}")
            AppInfoText(text = "Puno ime: ${state.authData.fullName ?: "-"}")
            AppInfoText(text = "Broj favorita: ${state.favoriteCount}")
            AppInfoText(text = "Broj watchlist filmova: ${state.watchlistCount}")

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(text = "Nazad") }
            OutlinedButton(onClick = { onIntent(ProfileIntent.Logout) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Odjavi se")
            }
        }
    }
}
