package rs.edu.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val AUTH_DATA_STORE_FILE = "auth.preferences_pb"

fun createAuthDataStore(
    producePath: () -> String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}

expect fun createPlatformAuthDataStore(): DataStore<Preferences>
