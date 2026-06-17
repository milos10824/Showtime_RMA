package rs.edu.raf.showtime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rs.edu.raf.showtime.feature.quiz.QuizCompletionGuard

class QuizCompletionGuardTest {

    @Test
    fun quizResultCanOnlyBeCommittedOncePerSession() {
        val guard = QuizCompletionGuard()

        assertTrue(guard.tryComplete())
        assertFalse(guard.tryComplete())
    }

    @Test
    fun newSessionAllowsANewCompletion() {
        val guard = QuizCompletionGuard()
        guard.tryComplete()

        guard.reset()

        assertTrue(guard.tryComplete())
    }
}
