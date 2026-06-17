package rs.edu.raf.showtime.feature.saved

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SavedMoviesScreen(
    state: SavedMoviesState,
    onIntent: (SavedMoviesIntent) -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = if (state.type == SavedListType.FAVORITE) "Favorite" else "Watchlist")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { onIntent(SavedMoviesIntent.Refresh) }) { Text(text = "Osveži") }
                OutlinedButton(onClick = { onIntent(SavedMoviesIntent.BackClicked) }) { Text(text = "Nazad") }
            }

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }
            if (state.isEmpty) {
                EmptyContent(message = "Lista je prazna.")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = state.movies, key = { movie -> movie.imdbId }) { movie ->
                    SavedMovieItem(
                        movie = movie,
                        onClick = { onIntent(SavedMoviesIntent.MovieClicked(movie.imdbId)) },
                        onRemove = { onIntent(SavedMoviesIntent.RemoveClicked(movie.imdbId)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedMovieItem(
    movie: MovieListItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MovieImage(
                imagePath = movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier.width(88.dp).height(132.dp),
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
                    }.ifBlank { "Detalji nisu dostupni" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Ukloni")
                }
            }
        }
    }
}
