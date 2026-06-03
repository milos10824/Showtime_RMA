package rs.edu.raf.showtime.feature.quiz

enum class QuizQuestionType {
    GUESS_MOVIE,
    GUESS_YEAR,
    GUESS_ACTOR,
}

data class QuizQuestion(
    val movieId: String,
    val type: QuizQuestionType,
    val title: String,
    val imagePath: String?,
    val correctAnswer: String,
    val options: List<String>,
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val selectedAnswer: String? = null,
    val correctAnswer: String? = null,
    val canAnswer: Boolean = true,
    val score: Double = 0.0,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val timeLeft: Int = 60,
    val usedTime: Int = 0,
    val isLoading: Boolean = false,
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val showAbandonDialog: Boolean = false,
    val error: String? = null,
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val wrongCount: Int
        get() = if (isFinished) questions.size - correctCount else currentIndex - correctCount
}

sealed interface QuizIntent {
    data object Start : QuizIntent
    data class AnswerClicked(val answer: String) : QuizIntent
    data object AbandonClicked : QuizIntent
    data object CancelAbandon : QuizIntent
    data object ConfirmAbandon : QuizIntent
}
