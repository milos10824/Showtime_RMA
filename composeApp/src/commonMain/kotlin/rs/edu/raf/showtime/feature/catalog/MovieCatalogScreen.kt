package rs.edu.raf.showtime.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.EmptyContent
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.domain.movie.MovieListItem

@Composable
fun MovieCatalogScreen(
    state: MovieCatalogState,
    onIntent: (MovieCatalogIntent) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTitle(text = "Katalog filmova")

            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(MovieCatalogIntent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pretraga") },
                singleLine = true,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onIntent(MovieCatalogIntent.Refresh) },
                ) {
                    Text(text = "Osveži")
                }

                OutlinedButton(onClick = onBack) {
                    Text(text = "Nazad")
                }
            }

            if (state.isLoading) {
                LoadingContent()
            }

            state.error?.let { message ->
                ErrorContent(message = message)
            }

            if (!state.isLoading && state.movies.isEmpty()) {
                EmptyContent(message = "Nema filmova za prikaz.")
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = state.movies,
                    key = { movie -> movie.imdbId },
                ) { movie ->
                    MovieCatalogItem(
                        movie = movie,
                        onFavoriteClick = {
                            onIntent(
                                MovieCatalogIntent.FavoriteChanged(
                                    movieId = movie.imdbId,
                                    value = !movie.isFavorite,
                                )
                            )
                        },
                        onWatchlistClick = {
                            onIntent(
                                MovieCatalogIntent.WatchlistChanged(
                                    movieId = movie.imdbId,
                                    value = !movie.isWatchlisted,
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieCatalogItem(
    movie: MovieListItem,
    onFavoriteClick: () -> Unit,
    onWatchlistClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = movie.title)

            Text(
                text = buildString {
                    movie.year?.let { append("Godina: $it") }
                    movie.imdbRating?.let {
                        if (isNotBlank()) append(" | ")
                        append("IMDb: $it")
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onFavoriteClick) {
                    Text(
                        text = if (movie.isFavorite) {
                            "Ukloni favorite"
                        } else {
                            "Favorite"
                        }
                    )
                }

                OutlinedButton(onClick = onWatchlistClick) {
                    Text(
                        text = if (movie.isWatchlisted) {
                            "Ukloni watchlist"
                        } else {
                            "Watchlist"
                        }
                    )
                }
            }
        }
    }
}
