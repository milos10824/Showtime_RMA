package rs.edu.raf.showtime.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.EmptyContent
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent

@Composable
fun MovieDetailsScreen(
    state: MovieDetailsState,
    onIntent: (MovieDetailsIntent) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val movie = state.movie
            AppTitle(text = movie?.title ?: "Detalji filma")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIntent(MovieDetailsIntent.Refresh) }) { Text(text = "Osveži") }
                OutlinedButton(onClick = onBack) { Text(text = "Nazad") }
            }

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }
            if (movie == null && !state.isLoading) EmptyContent(message = "Nema detalja za izabrani film.")

            movie?.let { item ->
                AppInfoText(text = "Godina: ${item.year ?: "-"}")
                AppInfoText(text = "Trajanje: ${item.runtime ?: "-"} min")
                AppInfoText(text = "IMDb: ${item.imdbRating ?: "-"}")
                AppInfoText(text = "Žanrovi: ${item.genres.joinToString(", ").ifBlank { "-" }}")
                AppInfoText(text = "Glumci: ${item.castNames.joinToString(", ").ifBlank { "-" }}")
                AppInfoText(text = item.overview ?: "Opis nije dostupan.")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onIntent(MovieDetailsIntent.FavoriteChanged(item.imdbId, !item.isFavorite)) },
                    ) {
                        Text(text = if (item.isFavorite) "Ukloni favorite" else "Favorite")
                    }

                    OutlinedButton(
                        onClick = { onIntent(MovieDetailsIntent.WatchlistChanged(item.imdbId, !item.isWatchlisted)) },
                    ) {
                        Text(text = if (item.isWatchlisted) "Ukloni watchlist" else "Watchlist")
                    }
                }
            }
        }
    }
}
