package rs.edu.raf.showtime.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.showtime.data.quiz.QuizRepository
import rs.edu.raf.showtime.domain.movie.MovieDetails

class QuizViewModel(
    private val repository: QuizRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var answerJob: Job? = null
    private val completionGuard = QuizCompletionGuard()
    private val events = MutableSharedFlow<QuizIntent>()
    private val _effect = MutableSharedFlow<QuizEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeEvents()
        viewModelScope.launch {
            repository.observeStats().collect { stats ->
                _state.value = QuizReducer.statsLoaded(_state.value, stats)
            }
        }
    }

    fun onIntent(intent: QuizIntent) {
        viewModelScope.launch { events.emit(intent) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { intent -> handleIntent(intent) }
        }
    }

    private fun handleIntent(intent: QuizIntent) {
        when (intent) {
            QuizIntent.Start -> startQuiz()
            is QuizIntent.AnswerClicked -> answer(intent.answer)
            QuizIntent.AbandonClicked -> _state.value = QuizReducer.abandonRequested(_state.value)
            QuizIntent.CancelAbandon -> _state.value = QuizReducer.abandonCancelled(_state.value)
            QuizIntent.ConfirmAbandon -> {
                if (_state.value.isStarted && !_state.value.isFinished) {
                    abandonQuiz(closeScreen = true)
                }
            }
            QuizIntent.BackClicked -> {
                if (_state.value.isStarted && !_state.value.isFinished) {
                    _state.value = QuizReducer.abandonRequested(_state.value)
                } else {
                    viewModelScope.launch { _effect.emit(QuizEffect.Close) }
                }
            }
        }
    }

    private fun startQuiz() {
        timerJob?.cancel()
        answerJob?.cancel()
        completionGuard.reset()

        viewModelScope.launch {
            _state.value = QuizReducer.loading(_state.value)

            try {
                val movies = repository.getQuizMovies(limit = 100)
                    .filter { it.year != null && (!it.posterPath.isNullOrBlank() || !it.backdropPath.isNullOrBlank()) }
                    .distinctBy { it.imdbId }

                if (movies.size < 10) {
                    _state.value = QuizReducer.error(
                        state = _state.value,
                        message = "Za pokretanje kviza potrebno je najmanje 10 lokalno sačuvanih filmova sa slikom.",
                    )
                    return@launch
                }

                val questions = createQuestions(movies)

                if (questions.size < 10) {
                    _state.value = QuizReducer.error(
                        state = _state.value,
                        message = "Nema dovoljno lokalnih podataka za sva tri tipa pitanja. Otvori još detalja filmova.",
                    )
                    return@launch
                }

                _state.value = QuizReducer.started(_state.value, questions)

                startTimer()
            } catch (error: Exception) {
                _state.value = if (error is ClientRequestException) {
                    QuizReducer.error(_state.value, "Server nije vratio podatke potrebne za kviz.")
                } else {
                    QuizReducer.offline(
                        state = _state.value,
                        message = "Kviz nije pokrenut. Proveri katalog i internet konekciju.",
                    )
                }
            }
        }
    }

    private fun answer(answer: String) {
        val currentState = _state.value
        if (!currentState.canAnswer || currentState.isFinished) return

        val question = currentState.currentQuestion ?: return
        val isCorrect = answer == question.correctAnswer

        _state.value = QuizReducer.answerSelected(
            state = currentState,
            answer = answer,
            correctAnswer = question.correctAnswer,
            isCorrect = isCorrect,
        )

        answerJob?.cancel()
        answerJob = viewModelScope.launch {
            delay(700)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        if (!currentState.isStarted || currentState.isFinished) return
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.questions.size) {
            finishQuiz()
            return
        }

        _state.value = QuizReducer.nextQuestion(currentState, nextIndex)
    }

    private fun finishQuiz() {
        val currentState = _state.value
        if (!currentState.isStarted || currentState.isFinished || !completionGuard.tryComplete()) return

        timerJob?.cancel()
        answerJob?.cancel()
        val score = calculateScore(
            correctCount = currentState.correctCount,
            timeLeft = currentState.timeLeft,
        )

        _state.value = QuizReducer.finished(currentState, score)

        viewModelScope.launch { repository.saveResult(score) }
    }

    private fun abandonQuiz(closeScreen: Boolean) {
        if (!_state.value.isStarted || _state.value.isFinished) return
        timerJob?.cancel()
        answerJob?.cancel()
        completionGuard.reset()
        _state.value = QuizReducer.abandoned(_state.value)
        if (closeScreen) {
            viewModelScope.launch { _effect.emit(QuizEffect.Close) }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0 && _state.value.isStarted) {
                delay(1000)
                val current = _state.value
                if (!current.isStarted || current.isFinished) break

                val newTime = current.timeLeft - 1
                _state.value = QuizReducer.timeTick(current, newTime)

                if (newTime <= 0) {
                    finishQuiz()
                    break
                }
            }
        }
    }

    private fun createQuestions(movies: List<MovieDetails>): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val usedMovieIds = mutableSetOf<String>()
        val usedImages = mutableSetOf<String>()
        val typeCounts = QuizQuestionType.entries.associateWith { 0 }.toMutableMap()
        val plannedTypes = listOf(
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

        while (questions.size < 10) {
            val preferredType = plannedTypes.getOrNull(questions.size)
            val limitedTypes = buildList {
                preferredType
                    ?.takeIf { (typeCounts[it] ?: 0) < 4 }
                    ?.let { add(it) }
                addAll(
                    QuizQuestionType.entries
                        .filter { it != preferredType && (typeCounts[it] ?: 0) < 4 }
                        .shuffled()
                )
            }

            val picked = findQuestion(
                typeOrder = limitedTypes,
                movies = movies,
                usedMovieIds = usedMovieIds,
                usedImages = usedImages,
            ) ?: break

            questions.add(picked)
            usedMovieIds.add(picked.movieId)
            picked.imagePath?.let { usedImages.add(it) }
            typeCounts[picked.type] = (typeCounts[picked.type] ?: 0) + 1
        }

        return questions
    }

    private fun findQuestion(
        typeOrder: List<QuizQuestionType>,
        movies: List<MovieDetails>,
        usedMovieIds: Set<String>,
        usedImages: Set<String>,
    ): QuizQuestion? {
        typeOrder.forEach { type ->
            movies.shuffled()
                .filter { it.imdbId !in usedMovieIds }
                .forEach { movie ->
                    val question = when (type) {
                        QuizQuestionType.GUESS_MOVIE -> createMovieTitleQuestion(movie, movies, usedImages)
                        QuizQuestionType.GUESS_YEAR -> createYearQuestion(movie)
                        QuizQuestionType.GUESS_ACTOR -> createActorQuestion(movie, movies)
                    } ?: return@forEach

                    val imagePath = question.imagePath?.trim()
                    val options = question.options.distinct()

                    if (
                        !imagePath.isNullOrBlank() &&
                        imagePath !in usedImages &&
                        options.size == 4 &&
                        question.correctAnswer in options
                    ) {
                        return question.copy(
                            imagePath = imagePath,
                            options = options.shuffled(),
                        )
                    }
                }
        }

        return null
    }

    private fun createMovieTitleQuestion(
        movie: MovieDetails,
        movies: List<MovieDetails>,
        usedImages: Set<String>,
    ): QuizQuestion? {
        val imagePath = QuizRules.selectMovieImage(movie, usedImages)
        if (imagePath.isNullOrBlank()) return null

        val wrongAnswers = movies
            .filter { it.imdbId != movie.imdbId }
            .map { it.title }
            .distinct()
            .shuffled()
            .take(3)

        if (wrongAnswers.size < 3) return null

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_MOVIE,
            title = movie.title,
            imagePath = imagePath,
            correctAnswer = movie.title,
            options = wrongAnswers + movie.title,
        )
    }

    private fun createYearQuestion(movie: MovieDetails): QuizQuestion? {
        val correctYear = movie.year ?: return null
        val posterPath = movie.posterPath ?: return null
        val options = mutableSetOf(correctYear)

        (1..10)
            .flatMap { offset -> listOf(correctYear - offset, correctYear + offset) }
            .filter { it > 1870 }
            .shuffled()
            .forEach { year ->
                if (options.size < 4) {
                    options.add(year)
                }
            }

        if (options.size < 4) return null

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_YEAR,
            title = movie.title,
            imagePath = posterPath,
            correctAnswer = correctYear.toString(),
            options = options.map { it.toString() },
        )
    }

    private fun createActorQuestion(movie: MovieDetails, movies: List<MovieDetails>): QuizQuestion? {
        val posterPath = movie.posterPath ?: return null
        val allTargetActors = movie.castNames.distinct()
        val correctActor = allTargetActors.take(3).randomOrNull() ?: return null

        val wrongActors = QuizRules.selectWrongActors(movie, movies)

        if (wrongActors.size < 3) return null

        return QuizQuestion(
            movieId = movie.imdbId,
            type = QuizQuestionType.GUESS_ACTOR,
            title = movie.title,
            imagePath = posterPath,
            correctAnswer = correctActor,
            options = wrongActors + correctActor,
        )
    }

    private fun calculateScore(correctCount: Int, timeLeft: Int): Double {
        return QuizRules.calculateScore(correctCount, timeLeft)
    }
}
