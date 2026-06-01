package rs.edu.raf.showtime.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.edu.raf.showtime.core.ui.AppInfoText
import rs.edu.raf.showtime.core.ui.AppScreen
import rs.edu.raf.showtime.core.ui.AppTitle
import rs.edu.raf.showtime.core.ui.ErrorContent
import rs.edu.raf.showtime.core.ui.LoadingContent

@Composable
fun QuizScreen(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
    onBack: () -> Unit,
) {
    AppScreen {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle(text = "Kviz")
            AppInfoText(text = "Najbolji rezultat: ${state.bestScore}")
            AppInfoText(text = "Broj odigranih kvizova: ${state.playedCount}")

            if (state.isLoading) LoadingContent()
            state.error?.let { ErrorContent(message = it) }

            if (state.questions.isEmpty() && !state.isLoading) {
                Button(onClick = { onIntent(QuizIntent.Start) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Pokreni kviz")
                }
            }

            if (state.isFinished) {
                AppInfoText(text = "Kviz je završen. Rezultat: ${state.score}/${state.questions.size}")
                Button(onClick = { onIntent(QuizIntent.Start) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Igraj ponovo")
                }
            }

            val question = state.currentQuestion
            if (question != null && !state.isFinished) {
                AppInfoText(text = "Pitanje ${state.currentIndex + 1}/${state.questions.size}")
                AppInfoText(text = "Koje godine je izašao film: ${question.title}?")

                question.options.forEach { year ->
                    OutlinedButton(
                        onClick = { onIntent(QuizIntent.AnswerClicked(year)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = year.toString())
                    }
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Nazad")
            }
        }
    }
}
