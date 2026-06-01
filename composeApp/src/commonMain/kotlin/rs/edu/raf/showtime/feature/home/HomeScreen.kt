package rs.edu.raf.showtime.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.auth.AuthData
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle

@Composable
fun HomeScreen(
    authData: AuthData,
    onOpenCatalog: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenQuiz: () -> Unit,
    onLogout: () -> Unit,
) {
    AppScreen {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTitle(text = "Showtime")

            AppInfoText(text = "Prijavljen korisnik: ${authData.username ?: "nepoznato"}")

            Button(onClick = onOpenCatalog, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Katalog filmova")
            }

            Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Profil")
            }

            Button(onClick = onOpenQuiz, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Kviz")
            }

            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Odjavi se")
            }
        }
    }
}
