package rs.edu.raf.showtime.feature.quiz

import rs.edu.raf.showtime.domain.quiz.QuizStats

object QuizReducer {

    fun statsLoaded(state: QuizState, stats: QuizStats): QuizState {
        return state.copy(
            bestScore = stats.bestScore,
            playedCount = stats.playedCount,
        )
    }

    fun loading(state: QuizState): QuizState {
        return state.copy(
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
            isOffline = false,
            error = null,
        )
    }

    fun started(state: QuizState, questions: List<QuizQuestion>): QuizState {
        return state.copy(
            questions = questions,
            isLoading = false,
            isStarted = true,
            isOffline = false,
            error = null,
        )
    }

    fun error(state: QuizState, message: String): QuizState {
        return state.copy(isLoading = false, error = message)
    }

    fun offline(state: QuizState, message: String): QuizState {
        return state.copy(isLoading = false, isOffline = true, error = message)
    }

    fun answerSelected(
        state: QuizState,
        answer: String,
        correctAnswer: String,
        isCorrect: Boolean,
    ): QuizState {
        return state.copy(
            selectedAnswer = answer,
            correctAnswer = correctAnswer,
            canAnswer = false,
            correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount,
        )
    }

    fun nextQuestion(state: QuizState, nextIndex: Int): QuizState {
        return state.copy(
            currentIndex = nextIndex,
            selectedAnswer = null,
            correctAnswer = null,
            canAnswer = true,
        )
    }

    fun timeTick(state: QuizState, timeLeft: Int): QuizState {
        return state.copy(timeLeft = timeLeft)
    }

    fun finished(state: QuizState, score: Double): QuizState {
        return state.copy(
            score = score,
            usedTime = 60 - state.timeLeft,
            isFinished = true,
            isStarted = false,
            canAnswer = false,
            showAbandonDialog = false,
            selectedAnswer = null,
            correctAnswer = null,
        )
    }

    fun abandonRequested(state: QuizState): QuizState {
        return state.copy(showAbandonDialog = true)
    }

    fun abandonCancelled(state: QuizState): QuizState {
        return state.copy(showAbandonDialog = false)
    }

    fun abandoned(state: QuizState): QuizState {
        return state.copy(
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
}
