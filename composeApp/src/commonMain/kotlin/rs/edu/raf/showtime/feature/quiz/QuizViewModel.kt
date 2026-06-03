package rs.edu.raf.showtime.feature.quiz

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.quiz.QuizRepository
import rs.edu.raf.showtime.domain.movie.MovieDetails
import kotlin.math.min

class QuizViewModel(
    private val repository: QuizRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var timerJob: Job? = null

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
            is QuizIntent.AnswerClicked -> answer(intent.answer)
            QuizIntent.AbandonClicked -> _state.value = _state.value.copy(showAbandonDialog = true)
            QuizIntent.CancelAbandon -> _state.value = _state.value.copy(showAbandonDialog = false)
            QuizIntent.ConfirmAbandon -> abandonQuiz()
        }
    }

    private fun startQuiz() {
        timerJob?.cancel()

        scope.launch {
            _state.value = _state.value.copy(
                questions = emptyList(),
                currentIndex = 0,
                correctCount = 0,
                selectedAnswer = null,
                correctAnswer = null,
                canAnswer = true,
                score = 0.0,
                timeLeft = 60,
                usedTime = 0,
                isLoading = true,
                isStarted = false,
                isFinished = false,
                showAbandonDialog = false,
                error = null,
            )

            try {
                val movies = repository.getQuizMovies(limit = 100)
                    .filter { it.year != null && (!it.posterPath.isNullOrBlank() || !it.backdropPath.isNullOrBlank()) }
                    .distinctBy { it.imdbId }

                if (movies.size < 10) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Browse the catalog first to populate your quiz pool.",
                    )
                    return@launch
                }

                val questions = createQuestions(movies)

                _state.value = _state.value.copy(
                    questions = questions,
                    isLoading = false,
                    isStarted = true,
                    error = null,
                )

                startTimer()
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Kviz nije pokrenut. Proveri katalog i internet konekciju.",
                )
            }
        }
    }

    private fun answer(answer: String) {
        val currentState = _state.value
        if (!currentState.canAnswer || currentState.isFinished) return

        val question = currentState.currentQuestion ?: return
        val isCorrect = answer == question.correctAnswer

        _state.value = currentState.copy(
            selectedAnswer = answer,
            correctAnswer = question.correctAnswer,
            canAnswer = false,
            correctCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount,
        )

        scope.launch {
            delay(700)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.questions.size) {
            finishQuiz()
            return
        }

        _state.value = currentState.copy(
            currentIndex = nextIndex,
            selectedAnswer = null,
            correctAnswer = null,
            canAnswer = true,
        )
    }

    private fun finishQuiz() {
        timerJob?.cancel()

        val currentState = _state.value
        val score = calculateScore(
            correctCount = currentState.correctCount,
            timeLeft = currentState.timeLeft,
        )

        _state.value = currentState.copy(
            score = score,
            usedTime = 60 - currentState.timeLeft,
            isFinished = true,
            isStarted = false,
            canAnswer = false,
            selectedAnswer = null,
            correctAnswer = null,
        )

        scope.launch { repository.saveResult(score) }
    }

    private fun abandonQuiz() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            questions = emptyList(),
            currentIndex = 0,
            selectedAnswer = null,
            correctAnswer = null,
            canAnswer = true,
            isStarted = false,
            isFinished = false,
            showAbandonDialog = false,
            timeLeft = 60,
            error = null,
        )
    }

    private fun startTimer() {
        timerJob = scope.launch {
            while (_state.value.timeLeft > 0 && _state.value.isStarted) {
                delay(1000)
                val current = _state.value
                if (!current.isStarted || current.isFinished) break

                val newTime = current.timeLeft - 1
                _state.value = current.copy(timeLeft = newTime)

                if (newTime <= 0) {
                    finishQuiz()
                    break
                }
            }
        }
    }

    private fun createQuestions(movies: List<MovieDetails>): List<QuizQuestion> {
        val pickedMovies = movies.shuffled().take(10)
        val types = listOf(
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_MOVIE,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_YEAR,
            QuizQuestionType.GUESS_ACTOR,
            QuizQuestionType.GUESS_ACTOR,
            QuizQuestionType.GUESS_ACTOR,
        ).shuffled()

        return pickedMovies.mapIndexed { index, movie ->
            when (types[index]) {
                QuizQuestionType.GUESS_MOVIE -> createMovieTitleQuestion(movie, movies)
                QuizQuestionType.GUESS_YEAR -> createYearQuestion(movie)
                QuizQuestionType.GUESS_ACTOR -> createActorQuestion(movie, movies) ?: createMovieTitleQuestion(movie, movies)
            }
        }
    }

    private fun createMovieTitleQuestion(movie: MovieDetails, movies: List<MovieDetails>): QuizQuestion {
        val wrongAnswers = movies
            .filter { it.imdbId != movie.imdbId }
            .map { it.title }
            .distinct()
            .shuffled()
            .take(3)

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_MOVIE,
            title = movie.title,
            imagePath = movie.backdropPath ?: movie.posterPath,
            correctAnswer = movie.title,
            options = (wrongAnswers + movie.title).shuffled(),
        )
    }

    private fun createYearQuestion(movie: MovieDetails): QuizQuestion {
        val correctYear = movie.year ?: 2000
        val options = mutableSetOf(correctYear)
        var offset = 1

        while (options.size < 4) {
            options.add(correctYear + offset)
            options.add(correctYear - offset)
            offset += 2
        }

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_YEAR,
            title = movie.title,
            imagePath = movie.posterPath,
            correctAnswer = correctYear.toString(),
            options = options.map { it.toString() }.shuffled(),
        )
    }

    private fun createActorQuestion(movie: MovieDetails, movies: List<MovieDetails>): QuizQuestion? {
        val actors = movie.castNames.distinct().take(3)
        val correctActor = actors.shuffled().firstOrNull() ?: return null

        val wrongActors = movies
            .filter { it.imdbId != movie.imdbId }
            .flatMap { it.castNames }
            .filter { it !in actors }
            .distinct()
            .shuffled()
            .take(3)

        if (wrongActors.size < 3) return null

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_ACTOR,
            title = movie.title,
            imagePath = movie.posterPath,
            correctAnswer = correctActor,
            options = (wrongActors + correctActor).shuffled(),
        )
    }

    private fun calculateScore(correctCount: Int, timeLeft: Int): Double {
        val result = correctCount * (9.0 + timeLeft / 60.0)
        return min(100.0, result)
    }
}
