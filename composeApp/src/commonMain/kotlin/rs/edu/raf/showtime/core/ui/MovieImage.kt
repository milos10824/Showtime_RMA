package rs.edu.raf.showtime.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MovieImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val imageUrl = remember(imagePath) {
        buildMovieImageUrl(imagePath)
    }

    var failed by remember(imageUrl) {
        mutableStateOf(false)
    }

    if (imageUrl == null || failed) {
        MovieImagePlaceholder(
            text = if (imageUrl == null) "Nema slike" else "Slika nije učitana",
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            onError = {
                failed = true
            },
        )
    }
}

@Composable
private fun MovieImagePlaceholder(
    text: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text)
    }
}

private fun buildMovieImageUrl(path: String?): String? {
    val value = path?.trim().orEmpty()

    if (value.isBlank()) {
        return null
    }

    if (value.startsWith("http")) {
        return value
    }

    val cleanPath = value.removePrefix("/")
    return "https://image.tmdb.org/t/p/w500/$cleanPath"
}
