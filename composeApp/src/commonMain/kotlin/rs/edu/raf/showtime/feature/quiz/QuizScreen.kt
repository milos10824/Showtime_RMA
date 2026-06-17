package rs.edu.raf.showtime.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent
import rs.edu.raf.showtime.core.ui.MovieImage
import rs.edu.raf.showtime.core.ui.PlatformBackHandler
import kotlin.math.roundToInt

@Composable
fun QuizScreen(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
) {
    PlatformBackHandler(
        enabled = state.isStarted && !state.isFinished,
        onBack = { onIntent(QuizIntent.BackClicked) },
    )

    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = "Kviz")
            AppInfoText(text = "Najbolji skor: ${formatScore(state.bestScore)}")
            AppInfoText(text = "Odigrano kvizova: ${state.playedCount}")

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }

            if (!state.isStarted && !state.isFinished && !state.isLoading) {
                Button(onClick = { onIntent(QuizIntent.Start) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Pokreni kviz")
                }
                OutlinedButton(
                    onClick = { onIntent(QuizIntent.BackClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Nazad")
                }
            }

            if (state.isFinished) {
                ResultContent(state = state)
                Button(onClick = { onIntent(QuizIntent.Start) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Igraj ponovo")
                }
                OutlinedButton(
                    onClick = { onIntent(QuizIntent.BackClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Nazad")
                }
            }

            val question = state.currentQuestion
            if (question != null && state.isStarted && !state.isFinished) {
                QuestionContent(
                    state = state,
                    question = question,
                    onAnswer = { answer -> onIntent(QuizIntent.AnswerClicked(answer)) },
                    onAbandon = { onIntent(QuizIntent.AbandonClicked) },
                )
            }
        }
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(QuizIntent.CancelAbandon) },
            title = { Text(text = "Odustati od kviza?") },
            text = { Text(text = "Dosadašnji rezultat neće biti sačuvan.") },
            confirmButton = {
                Button(
                    onClick = {
                        onIntent(QuizIntent.ConfirmAbandon)
                    },
                ) {
                    Text(text = "Odustani")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onIntent(QuizIntent.CancelAbandon) }) {
                    Text(text = "Nastavi kviz")
                }
            },
        )
    }
}

@Composable
private fun QuestionContent(
    state: QuizState,
    question: QuizQuestion,
    onAnswer: (String) -> Unit,
    onAbandon: () -> Unit,
) {
    AppInfoText(text = "Vreme: ${state.timeLeft}s")
    AppInfoText(text = "Pitanje ${state.currentIndex + 1}/${state.questions.size}")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = questionText(question),
                fontWeight = FontWeight.Bold,
            )

            MovieImage(
                imagePath = question.imagePath,
                contentDescription = if (question.type == QuizQuestionType.GUESS_MOVIE) {
                    "Slika filma za pogađanje"
                } else {
                    question.title
                },
                modifier = Modifier.fillMaxWidth().height(170.dp),
            )

            question.options.forEach { answer ->
                OutlinedButton(
                    onClick = { onAnswer(answer) },
                    enabled = state.canAnswer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = optionText(answer, state))
                }
            }
        }
    }

    OutlinedButton(onClick = onAbandon, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Odustani")
    }
}

@Composable
private fun ResultContent(state: QuizState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Rezultat", fontWeight = FontWeight.Bold)
            Text(text = "Skor: ${formatScore(state.score)}/100")
            Text(text = "Tačno: ${state.correctCount}")
            Text(text = "Netačno: ${state.wrongCount}")
            Text(text = "Iskorišćeno vreme: ${state.usedTime}s")
        }
    }
}

private fun questionText(question: QuizQuestion): String {
    return when (question.type) {
        QuizQuestionType.GUESS_MOVIE -> "Koji je ovo film?"
        QuizQuestionType.GUESS_YEAR -> "Koje godine je izašao film: ${question.title}?"
        QuizQuestionType.GUESS_ACTOR -> "Ko glumi u filmu: ${question.title}?"
    }
}

private fun optionText(answer: String, state: QuizState): String {
    if (state.selectedAnswer == null) return answer

    return when {
        answer == state.correctAnswer -> "$answer  ✓"
        answer == state.selectedAnswer -> "$answer  ✕"
        else -> answer
    }
}

private fun formatScore(score: Double): String {
    val cents = (score * 100).roundToInt()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}
