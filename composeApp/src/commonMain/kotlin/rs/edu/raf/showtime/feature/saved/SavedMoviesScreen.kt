package rs.edu.raf.showtime.feature.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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

@Composable
fun SavedMoviesScreen(
    state: SavedMoviesState,
    onIntent: (SavedMoviesIntent) -> Unit,
    onMovieClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = if (state.type == SavedListType.FAVORITES) "Favorite" else "Watchlist")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIntent(SavedMoviesIntent.Refresh) }) { Text(text = "Osveži") }
                OutlinedButton(onClick = onBack) { Text(text = "Nazad") }
            }

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }
            if (!state.isLoading && state.movies.isEmpty()) {
                EmptyContent(message = "Lista je prazna.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = state.movies, key = { movie -> movie.imdbId }) { movie ->
                    SavedMovieItem(
                        movie = movie,
                        onClick = { onMovieClick(movie.imdbId) },
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
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MovieImage(
                imagePath = movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )

            Text(text = movie.title, fontWeight = FontWeight.Bold)
            Text(
                text = buildString {
                    movie.year?.let { append("Godina: $it") }
                    movie.imdbRating?.let {
                        if (isNotBlank()) append(" | ")
                        append("IMDb: $it")
                    }
                }
            )
            OutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Ukloni")
            }
        }
    }
}
