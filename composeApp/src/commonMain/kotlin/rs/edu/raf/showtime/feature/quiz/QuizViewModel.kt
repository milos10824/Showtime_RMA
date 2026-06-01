package rs.edu.raf.showtime.feature.quiz

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.quiz.QuizRepository

class QuizViewModel(
    private val repository: QuizRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.observeStats().collect { stats ->
                _state.value = _state.value.copy(
                    bestScore = stats.bestScore,
                    playedCount = stats.playedCount,
                )
            }
        }
    }

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            QuizIntent.Start -> startQuiz()
            is QuizIntent.AnswerClicked -> answer(intent.year)
        }
    }

    private fun startQuiz() {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                isFinished = false,
                error = null,
                score = 0,
                currentIndex = 0,
            )

            try {
                val movies = repository.getQuizMovies(limit = 30)
                    .filter { it.year != null }
                    .shuffled()
                    .take(5)

                if (movies.size < 4) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Nema dovoljno filmova za kviz. Prvo osveži katalog.",
                    )
                    return@launch
                }

                val questions = movies.mapNotNull { movie ->
                    val year = movie.year ?: return@mapNotNull null
                    QuizQuestion(
                        movieId = movie.imdbId,
                        title = movie.title,
                        correctYear = year,
                        options = createYearOptions(year),
                    )
                }

                _state.value = _state.value.copy(
                    questions = questions,
                    isLoading = false,
                    error = null,
                )
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Kviz nije pokrenut.",
                )
            }
        }
    }

    private fun answer(year: Int) {
        val currentState = _state.value
        val question = currentState.currentQuestion ?: return
        val newScore = if (year == question.correctYear) currentState.score + 1 else currentState.score
        val nextIndex = currentState.currentIndex + 1
        val finished = nextIndex >= currentState.questions.size

        _state.value = currentState.copy(
            score = newScore,
            currentIndex = nextIndex,
            isFinished = finished,
        )

        if (finished) {
            scope.launch { repository.saveResult(newScore) }
        }
    }

    private fun createYearOptions(correctYear: Int): List<Int> {
        val options = mutableSetOf(correctYear)
        var offset = 1
        while (options.size < 4) {
            options.add(correctYear + offset)
            options.add(correctYear - offset)
            offset += 1
        }
        return options.shuffled()
    }
}
