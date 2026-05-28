package rs.edu.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual fun createPlatformAuthDataStore(): DataStore<Preferences> {
    return createAuthDataStore {
        AndroidAppContext
            .requireContext()
            .filesDir
            .resolve(AUTH_DATA_STORE_FILE)
            .absolutePath
    }
}
