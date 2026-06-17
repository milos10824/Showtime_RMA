package rs.edu.raf.showtime.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val showtimeColorScheme = lightColorScheme(
    primary = Color(0xFF006D77),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F4F2),
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF8B2F5E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E8),
    onSecondaryContainer = Color(0xFF3A001F),
    tertiary = Color(0xFF9A6500),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDA8),
    onTertiaryContainer = Color(0xFF311B00),
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFE8EFEE),
    onSurfaceVariant = Color(0xFF3F4948),
)

@Composable
fun AppScreen(
    content: @Composable ColumnScope.() -> Unit
) {
    MaterialTheme(colorScheme = showtimeColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
            }
        }
    }
}
