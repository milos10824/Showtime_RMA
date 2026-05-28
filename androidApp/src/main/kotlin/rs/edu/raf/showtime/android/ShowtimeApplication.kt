package rs.edu.raf.showtime.android

import android.app.Application
import rs.edu.raf.showtime.core.auth.AndroidAppContext

class ShowtimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.init(this)
    }
}