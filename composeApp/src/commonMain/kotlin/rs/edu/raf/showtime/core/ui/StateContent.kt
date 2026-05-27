package rs.edu.raf.showtime.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoadingContent() {
    CircularProgressIndicator()
}

@Composable
fun ErrorContent(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun EmptyContent(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}
