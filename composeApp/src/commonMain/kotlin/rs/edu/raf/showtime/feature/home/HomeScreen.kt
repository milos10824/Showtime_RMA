package rs.edu.raf.showtime.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.EmptyContent
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.feature.saved.SavedListType

@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val authData = state.authData
    AppScreen {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppTitle(text = "Showtime")

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }
            if (state.isEmpty) EmptyContent(message = "Podaci o prijavljenom korisniku nisu dostupni.")

            if (!state.isLoading && !state.isEmpty) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = authData.fullName ?: authData.username ?: "Korisnik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Prijavljen kao ${authData.username ?: "nepoznato"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { onIntent(HomeIntent.OpenCatalog) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Katalog filmova")
                        }

                        Button(
                            onClick = { onIntent(HomeIntent.OpenSaved(SavedListType.FAVORITE)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Favorite")
                        }

                        Button(
                            onClick = { onIntent(HomeIntent.OpenSaved(SavedListType.WATCHLIST)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Watchlist")
                        }

                        Button(
                            onClick = { onIntent(HomeIntent.OpenQuiz) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Kviz")
                        }

                        Button(
                            onClick = { onIntent(HomeIntent.OpenProfile) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Profil")
                        }
                    }
                }
            }
        }
    }
}
