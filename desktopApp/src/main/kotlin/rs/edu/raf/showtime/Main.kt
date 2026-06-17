package rs.edu.raf.showtime

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import rs.edu.raf.showtime.di.initShowtimeKoin

fun main() {
    initShowtimeKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Showtime"
        ) {
            ShowtimeApp()
        }
    }
}
