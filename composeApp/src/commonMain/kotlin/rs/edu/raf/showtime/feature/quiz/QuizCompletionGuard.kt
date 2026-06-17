package rs.edu.raf.showtime.feature.quiz

internal class QuizCompletionGuard {
    private var completed = false

    fun tryComplete(): Boolean {
        if (completed) return false
        completed = true
        return true
    }

    fun reset() {
        completed = false
    }
}
