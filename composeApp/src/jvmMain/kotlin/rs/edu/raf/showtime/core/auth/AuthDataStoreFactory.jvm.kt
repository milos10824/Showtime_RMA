package rs.edu.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

actual fun createPlatformAuthDataStore(): DataStore<Preferences> {
    return createAuthDataStore {
        val appDir = File(System.getProperty("user.home"), ".showtime_rma")
        appDir.mkdirs()
        File(appDir, AUTH_DATA_STORE_FILE).absolutePath
    }
}
