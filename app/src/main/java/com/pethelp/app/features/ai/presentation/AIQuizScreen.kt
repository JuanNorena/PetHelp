package com.pethelp.app.features.ai.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.R

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val description: String = ""
)

private val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        id = "pet_type",
        question = "Que tipo de mascota prefieres?",
        options = listOf("Perro", "Gato", "Conejo", "Ave", "Otro"),
        description = "Elige el tipo de mascota que mas te interesa"
    ),
    QuizQuestion(
        id = "activity_level",
        question = "Cual es tu nivel de actividad?",
        options = listOf("Bajo", "Moderado", "Alto", "Muy activo"),
        description = "Esto ayuda a recomendar mascotas con energia compatible"
    ),
    QuizQuestion(
        id = "pet_size",
        question = "Que tamano de mascota prefieres?",
        options = listOf("Pequeno", "Mediano", "Grande", "Cualquiera"),
        description = "Considera tu espacio disponible"
    ),
    QuizQuestion(
        id = "experience",
        question = "Tienes experiencia previa con mascotas?",
        options = listOf("Si, mucha", "Algo", "No, es mi primera", "He tenido mascotas"),
        description = "Esto ayuda a ajustar la recomendacion a tu nivel"
    ),
    QuizQuestion(
        id = "living_space",
        question = "Donde vives?",
        options = listOf("Apartamento pequeno", "Apartamento grande", "Casa con patio", "Finca/Terreno"),
        description = "El espacio es importante para la salud de la mascota"
    ),
    QuizQuestion(
        id = "time_available",
        question = "Cuanto tiempo tienes disponible para una mascota?",
        options = listOf("Poco (menos de 1h/dia)", "Moderado (1-2h/dia)", "Bastante (2-4h/dia)", "Mucho (4+ horas)"),
        description = "Algunas mascotas requieren mas cuidado y atencion"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIQuizScreen(
    navController: NavController,
    viewModel: AiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalQuestions = QUIZ_QUESTIONS.size
    val currentQuestionIndex = uiState.currentQuestionIndex.coerceIn(0, totalQuestions - 1)
    val currentQuestion = QUIZ_QUESTIONS[currentQuestionIndex]
    val selectedAnswer = uiState.quizAnswers[currentQuestion.id]
    val answeredQuestions = uiState.quizAnswers.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Descubre tu mascota ideal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Pregunta ${currentQuestionIndex + 1} de $totalQuestions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> LoadingQuizContent(Modifier.padding(padding))
            uiState.showRecommendations -> {
                AIResultsScreen(
                    recommendations = uiState.recommendations,
                    navController = navController,
                    quizAnswers = uiState.quizAnswers,
                    onRestart = { viewModel.resetQuiz() }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        QuizProgressBar(
                            progress = (currentQuestionIndex + 1).toFloat() / totalQuestions
                        )
                    }

                    item {
                        QuestionCard(
                            question = currentQuestion,
                            selectedAnswer = selectedAnswer,
                            onAnswerSelected = { answer ->
                                viewModel.updateQuizAnswer(currentQuestion.id, answer)
                            }
                        )
                    }

                    item {
                        QuizActions(
                            canGoBack = currentQuestionIndex > 0,
                            canContinue = !selectedAnswer.isNullOrBlank(),
                            isLastQuestion = currentQuestionIndex == totalQuestions - 1,
                            error = uiState.error,
                            onBack = viewModel::goToPreviousQuestion,
                            onNext = {
                                if (currentQuestionIndex == totalQuestions - 1) {
                                    viewModel.submitQuiz()
                                } else {
                                    viewModel.goToNextQuestion(totalQuestions)
                                }
                            }
                        )
                    }

                    if (answeredQuestions > 0) {
                        item {
                            Text(
                                text = "Tus respuestas",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(QUIZ_QUESTIONS.filter { uiState.quizAnswers.containsKey(it.id) }) { question ->
                            AnswerSummary(
                                question = question.question,
                                answer = uiState.quizAnswers[question.id].orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingQuizContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun QuizProgressBar(progress: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun QuizActions(
    canGoBack: Boolean,
    canContinue: Boolean,
    isLastQuestion: Boolean,
    error: String?,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (canGoBack) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Atras", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp),
                enabled = canContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "Ver recomendaciones" else "Siguiente",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = question.question,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = question.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            question.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnswerSelected(option) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAnswer == option,
                        onClick = { onAnswerSelected(option) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerSummary(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = answer,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
