package rs.edu.raf.showtime.core.auth

import android.content.Context

object AndroidAppContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun requireContext(): Context {
        return checkNotNull(appContext) {
            "AndroidAppContext nije inicijalizovan."
        }
    }
}
