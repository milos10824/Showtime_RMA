package rs.edu.raf.showtime.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = "Profil")

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Korisnik", fontWeight = FontWeight.Bold)
                    AppInfoText(text = "Korisničko ime: ${state.authData.username ?: "-"}")
                    AppInfoText(text = "Puno ime: ${state.authData.fullName ?: "-"}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Lokalni podaci", fontWeight = FontWeight.Bold)
                    AppInfoText(text = "Favorite: ${state.favoriteCount}")
                    AppInfoText(text = "Watchlist: ${state.watchlistCount}")
                    AppInfoText(text = "Najbolji skor: ${formatScore(state.bestScore)}")
                    AppInfoText(text = "Broj kvizova: ${state.playedCount}")
                }
            }

            Button(onClick = { onIntent(ProfileIntent.Refresh) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Osveži profil")
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(text = "Nazad") }
            OutlinedButton(onClick = { onIntent(ProfileIntent.Logout) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Odjavi se")
            }
        }
    }
}

private fun formatScore(score: Double): String {
    val rounded = (score * 100).toInt() / 100.0
    return rounded.toString()
}
