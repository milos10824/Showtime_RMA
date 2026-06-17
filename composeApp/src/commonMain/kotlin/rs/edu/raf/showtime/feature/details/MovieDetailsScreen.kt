package rs.edu.raf.showtime.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import rs.edu.raf.showtime.core.ui.EmptyContent
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.core.ui.MovieImage

@Composable
fun MovieDetailsScreen(
    state: MovieDetailsState,
    onIntent: (MovieDetailsIntent) -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val movie = state.movie
            AppTitle(text = movie?.title ?: "Detalji filma")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIntent(MovieDetailsIntent.Refresh) }) { Text(text = "Osveži") }
                OutlinedButton(onClick = { onIntent(MovieDetailsIntent.BackClicked) }) { Text(text = "Nazad") }
            }

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }
            if (state.isEmpty) EmptyContent(message = "Nema detalja za izabrani film.")

            movie?.let { item ->
                item.backdropPath?.let { backdrop ->
                    MovieImage(
                        imagePath = backdrop,
                        contentDescription = "${item.title} backdrop",
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MovieImage(
                            imagePath = item.posterPath,
                            contentDescription = "${item.title} poster",
                            modifier = Modifier.width(110.dp).height(165.dp),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(text = item.title, fontWeight = FontWeight.Bold)
                            AppInfoText(text = "Godina: ${item.year ?: "-"}")
                            AppInfoText(text = "Trajanje: ${item.runtime ?: "-"} min")
                            AppInfoText(text = "IMDb: ${item.imdbRating ?: "-"}")
                            AppInfoText(text = "TMDB: ${item.tmdbRating ?: "-"}")
                            AppInfoText(text = "Žanrovi: ${item.genres.joinToString(", ").ifBlank { "-" }}")
                        }
                    }
                }

                AppInfoText(text = "Glumci: ${item.castNames.joinToString(", ").ifBlank { "-" }}")
                Text(text = item.overview ?: "Opis nije dostupan.")

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
