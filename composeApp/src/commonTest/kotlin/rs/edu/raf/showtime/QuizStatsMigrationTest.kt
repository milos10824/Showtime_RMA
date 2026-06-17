package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import rs.edu.raf.showtime.data.quiz.legacyQuizStatsOwnerUsername

class QuizStatsMigrationTest {

    @Test
    fun legacyStatsKeyMatchesPreviousUsernameHashStorage() {
        val username = "existing_user"

        assertEquals(
            "legacy_${username.hashCode()}",
            legacyQuizStatsOwnerUsername(username),
        )
    }
}
