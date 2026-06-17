package rs.edu.raf.showtime.navigation

import androidx.lifecycle.SavedStateHandle
import rs.edu.raf.showtime.feature.saved.SavedListType

object ShowtimeRoute {
    const val AUTH = "auth"
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val DETAILS = "details/{$MOVIE_ID}"
    const val SAVED = "saved/{$SAVED_TYPE}"
    const val PROFILE = "profile"
    const val QUIZ = "quiz"

    fun details(movieId: String): String = "details/$movieId"
    fun saved(type: SavedListType): String = "saved/${type.name.lowercase()}"
}

const val MOVIE_ID = "movieId"
const val SAVED_TYPE = "savedType"

inline val SavedStateHandle.movieIdOrThrow: String
    get() = get<String>(MOVIE_ID)
        ?: throw IllegalStateException("$MOVIE_ID is mandatory and can not be null")

inline val SavedStateHandle.savedListTypeOrThrow: SavedListType
    get() {
        val value = get<String>(SAVED_TYPE)
            ?: throw IllegalStateException("$SAVED_TYPE is mandatory and can not be null")

        return SavedListType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: throw IllegalStateException("$SAVED_TYPE has unsupported value: $value")
    }
