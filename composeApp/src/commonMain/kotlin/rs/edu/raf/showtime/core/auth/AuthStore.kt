package rs.edu.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthStore(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val username = stringPreferencesKey("username")
        val fullName = stringPreferencesKey("full_name")
    }

    val authData: Flow<AuthData> = dataStore.data.map { preferences ->
        AuthData(
            token = preferences[Keys.token],
            username = preferences[Keys.username],
            fullName = preferences[Keys.fullName],
        )
    }

    suspend fun saveAuthData(
        token: String,
        username: String,
        fullName: String?,
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.token] = token
            preferences[Keys.username] = username

            if (fullName.isNullOrBlank()) {
                preferences.remove(Keys.fullName)
            } else {
                preferences[Keys.fullName] = fullName
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
