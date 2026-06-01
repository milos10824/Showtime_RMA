package rs.edu.raf.showtime.feature.quiz

data class QuizQuestion(
    val movieId: String,
    val title: String,
    val correctYear: Int,
    val options: List<Int>,
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val bestScore: Int = 0,
    val playedCount: Int = 0,
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val error: String? = null,
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)
}

sealed interface QuizIntent {
    data object Start : QuizIntent
    data class AnswerClicked(val year: Int) : QuizIntent
}
