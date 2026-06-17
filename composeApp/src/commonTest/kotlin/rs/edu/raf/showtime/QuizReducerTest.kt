package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rs.edu.raf.showtime.feature.quiz.QuizReducer
import rs.edu.raf.showtime.feature.quiz.QuizState

class QuizReducerTest {

    @Test
    fun abandonedQuizIsNotMarkedAsFinishedOrScored() {
        val active = QuizState(
            isStarted = true,
            correctCount = 7,
            score = 65.0,
            showAbandonDialog = true,
        )

        val result = QuizReducer.abandoned(active)

        assertFalse(result.isStarted)
        assertFalse(result.isFinished)
        assertFalse(result.showAbandonDialog)
        assertTrue(result.questions.isEmpty())
    }

    @Test
    fun finishingQuizClosesAbandonDialog() {
        val active = QuizState(
            isStarted = true,
            showAbandonDialog = true,
            timeLeft = 0,
        )

        val result = QuizReducer.finished(active, score = 45.0)

        assertTrue(result.isFinished)
        assertFalse(result.isStarted)
        assertFalse(result.showAbandonDialog)
        assertEquals(45.0, result.score)
    }
}
