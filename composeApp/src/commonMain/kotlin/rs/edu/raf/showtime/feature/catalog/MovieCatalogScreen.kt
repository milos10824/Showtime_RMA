package rs.edu.raf.showtime.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import rs.edu.raf.showtime.core.ui.MovieImage
import rs.edu.raf.showtime.domain.movie.MovieListItem

private val genreFilters = listOf(
    GenreFilter(null, "Svi"),
    GenreFilter(28, "Action"),
    GenreFilter(35, "Comedy"),
    GenreFilter(18, "Drama"),
    GenreFilter(27, "Horror"),
    GenreFilter(53, "Thriller"),
)

private val sortOptions = listOf(
    "" to "Popularno",
    "imdb_rating" to "Ocena",
    "year" to "Godina",
    "title" to "Naziv",
)

@Composable
fun MovieCatalogScreen(
    state: MovieCatalogState,
    onIntent: (MovieCatalogIntent) -> Unit,
    onMovieClick: (String) -> Unit,
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

            Text(text = "Žanr")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genreFilters) { genre ->
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

            Text(text = "Sortiranje")
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
                item {
                    Button(onClick = { onIntent(MovieCatalogIntent.Refresh) }) {
                        Text(text = "Osveži")
                    }
                }
                item {
                    OutlinedButton(onClick = { onIntent(MovieCatalogIntent.PreviousPage) }) {
                        Text(text = "Prethodna")
                    }
                }
                item {
                    OutlinedButton(onClick = { onIntent(MovieCatalogIntent.NextPage) }) {
                        Text(text = "Sledeća")
                    }
                }
                item {
                    OutlinedButton(onClick = onBack) {
                        Text(text = "Nazad")
                    }
                }
            }

            Text(text = "Strana: ${state.page}")

            if (state.isLoading) {
                LoadingContent()
            }

            state.error?.let { message ->
                ErrorContent(message = message)
            }

            if (!state.isLoading && state.movies.isEmpty()) {
                EmptyContent(message = "Nema filmova za prikaz.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = state.movies, key = { movie -> movie.imdbId }) { movie ->
                    MovieCatalogItem(
                        movie = movie,
                        onClick = { onMovieClick(movie.imdbId) },
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MovieImage(
                imagePath = movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )

            Text(text = movie.title, fontWeight = FontWeight.Bold)

            Text(
                text = buildString {
                    movie.year?.let { append("Godina: $it") }
                    movie.imdbRating?.let {
                        if (isNotBlank()) append(" | ")
                        append("IMDb: $it")
                    }
                    if (movie.genres.isNotEmpty()) {
                        if (isNotBlank()) append(" | ")
                        append(movie.genres.take(2).joinToString(", "))
                    }
                }
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
