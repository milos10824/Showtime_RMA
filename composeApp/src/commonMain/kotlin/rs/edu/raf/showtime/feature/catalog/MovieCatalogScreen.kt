package rs.edu.raf.showtime.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.EmptyContent
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.core.ui.MovieImage
import rs.edu.raf.showtime.domain.movie.MovieListItem

private val sortOptions = listOf(
    "imdb_votes" to "IMDb glasovi",
    "imdb_rating" to "IMDb ocena",
    "tmdb_rating" to "TMDB ocena",
    "year" to "Godina",
    "title" to "Naziv",
)

private val sortOrderOptions = listOf(
    "desc" to "Opadajuće",
    "asc" to "Rastuće",
)

@Composable
fun MovieCatalogScreen(
    state: MovieCatalogState,
    onIntent: (MovieCatalogIntent) -> Unit,
) {
    AppScreen {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTitle(text = "Katalog filmova")

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
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onIntent(MovieCatalogIntent.SearchChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pretraga") },
                        singleLine = true,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.minYear,
                            onValueChange = { onIntent(MovieCatalogIntent.MinYearChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Od") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.maxYear,
                            onValueChange = { onIntent(MovieCatalogIntent.MaxYearChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Do") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.minRating,
                            onValueChange = { onIntent(MovieCatalogIntent.MinRatingChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Ocena") },
                            singleLine = true,
                        )
                    }

                    Text(
                        text = "Žanr",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.genres, key = { it.id ?: -1 }) { genre ->
                            val selected = genre == state.genre
                            if (selected) {
                                Button(onClick = { onIntent(MovieCatalogIntent.GenreChanged(genre)) }) {
                                    Text(text = genre.name)
                                }
                            } else {
                                OutlinedButton(onClick = { onIntent(MovieCatalogIntent.GenreChanged(genre)) }) {
                                    Text(text = genre.name)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Sortiranje",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sortOptions) { option ->
                            val selected = option.first == state.sortBy
                            if (selected) {
                                Button(onClick = { onIntent(MovieCatalogIntent.SortChanged(option.first)) }) {
                                    Text(text = option.second)
                                }
                            } else {
                                OutlinedButton(onClick = { onIntent(MovieCatalogIntent.SortChanged(option.first)) }) {
                                    Text(text = option.second)
                                }
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sortOrderOptions) { option ->
                            val selected = option.first == state.sortOrder
                            if (selected) {
                                Button(onClick = { onIntent(MovieCatalogIntent.SortOrderChanged(option.first)) }) {
                                    Text(text = option.second)
                                }
                            } else {
                                OutlinedButton(onClick = { onIntent(MovieCatalogIntent.SortOrderChanged(option.first)) }) {
                                    Text(text = option.second)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onIntent(MovieCatalogIntent.Refresh) },
                    enabled = !state.isLoading,
                ) {
                    Text(text = "Osveži")
                }
                OutlinedButton(
                    onClick = { onIntent(MovieCatalogIntent.PreviousPage) },
                    enabled = state.page > 1 && !state.isLoading,
                ) {
                    Text(text = "Prethodna")
                }
                OutlinedButton(
                    onClick = { onIntent(MovieCatalogIntent.NextPage) },
                    enabled = state.canGoNext && !state.isLoading,
                ) {
                    Text(text = "Sledeća")
                }
                OutlinedButton(onClick = { onIntent(MovieCatalogIntent.BackClicked) }) {
                    Text(text = "Nazad")
                }
            }

            Text(
                text = "Strana ${state.page}/${maxOf(1, state.totalPages)} · ukupno ${state.totalItems}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.isLoading) {
                LoadingContent()
            }

            state.error?.let { message ->
                ErrorContent(message = message)
            }

            state.genreError?.let { message ->
                ErrorContent(message = message)
            }

            if (state.isEmpty) {
                EmptyContent(message = "Nema filmova za prikaz.")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = state.movies, key = { movie -> movie.imdbId }) { movie ->
                    MovieCatalogItem(
                        movie = movie,
                        onClick = { onIntent(MovieCatalogIntent.MovieClicked(movie.imdbId)) },
                        onFavoriteClick = {
                            onIntent(MovieCatalogIntent.FavoriteChanged(movie.imdbId, !movie.isFavorite))
                        },
                        onWatchlistClick = {
                            onIntent(MovieCatalogIntent.WatchlistChanged(movie.imdbId, !movie.isWatchlisted))
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
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onWatchlistClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MovieImage(
                imagePath = movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(88.dp)
                    .height(132.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = buildString {
                        movie.year?.let { append(it) }
                        movie.imdbRating?.let {
                            if (isNotBlank()) append(" · ")
                            append("IMDb $it")
                        }
                        if (movie.genres.isNotEmpty()) {
                            if (isNotBlank()) append(" · ")
                            append(movie.genres.take(2).joinToString(", "))
                        }
                    }.ifBlank { "Detalji nisu dostupni" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedButton(onClick = onFavoriteClick) {
                            Text(text = if (movie.isFavorite) "Ukloni favorite" else "Favorite")
                        }
                    }
                    item {
                        OutlinedButton(onClick = onWatchlistClick) {
                            Text(text = if (movie.isWatchlisted) "Ukloni watchlist" else "Watchlist")
                        }
                    }
                }
            }
        }
    }
}
