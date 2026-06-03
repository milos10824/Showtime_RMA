package rs.edu.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class UserMovieStore(
    private val dataStore: DataStore<Preferences>,
) {
    private fun safeUsername(username: String): String {
        return username.ifBlank { "guest" }
            .replace("|", "_")
            .replace(" ", "_")
    }

    private fun favoritesKey(username: String) =
        stringPreferencesKey("favorites_${safeUsername(username)}")

    private fun watchlistKey(username: String) =
        stringPreferencesKey("watchlist_${safeUsername(username)}")

    private fun decodeIds(value: String?): Set<String> {
        if (value.isNullOrBlank()) {
            return emptySet()
        }

        return value
            .split("|")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun encodeIds(ids: Set<String>): String {
        return ids
            .filter { it.isNotBlank() }
            .sorted()
            .joinToString("|")
    }

    suspend fun getFavorites(username: String): Set<String> {
        val preferences = dataStore.data.first()
        return decodeIds(preferences[favoritesKey(username)])
    }

    suspend fun getWatchlist(username: String): Set<String> {
        val preferences = dataStore.data.first()
        return decodeIds(preferences[watchlistKey(username)])
    }

    suspend fun setFavorite(
        username: String,
        movieId: String,
        isFavorite: Boolean,
    ) {
        dataStore.edit { preferences ->
            val key = favoritesKey(username)
            val current = decodeIds(preferences[key]).toMutableSet()

            if (isFavorite) {
                current.add(movieId)
            } else {
                current.remove(movieId)
            }

            preferences[key] = encodeIds(current)
        }
    }

    suspend fun setWatchlisted(
        username: String,
        movieId: String,
        isWatchlisted: Boolean,
    ) {
        dataStore.edit { preferences ->
            val key = watchlistKey(username)
            val current = decodeIds(preferences[key]).toMutableSet()

            if (isWatchlisted) {
                current.add(movieId)
            } else {
                current.remove(movieId)
            }

            preferences[key] = encodeIds(current)
        }
    }

    suspend fun replaceFavorites(
        username: String,
        movieIds: Set<String>,
    ) {
        dataStore.edit { preferences ->
            preferences[favoritesKey(username)] = encodeIds(movieIds)
        }
    }

    suspend fun replaceWatchlist(
        username: String,
        movieIds: Set<String>,
    ) {
        dataStore.edit { preferences ->
            preferences[watchlistKey(username)] = encodeIds(movieIds)
        }
    }
}
