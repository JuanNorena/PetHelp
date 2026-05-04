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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pethelp.app.R
import androidx.compose.ui.res.stringResource

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val description: String = ""
)

private val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        id = "pet_type",
        question = "¿Qué tipo de mascota prefieres?",
        options = listOf("Perro", "Gato", "Conejo", "Ave", "Otro"),
        description = "Elige el tipo de mascota que más te interesa"
    ),
    QuizQuestion(
        id = "activity_level",
        question = "¿Cuál es tu nivel de actividad?",
        options = listOf("Bajo", "Moderado", "Alto", "Muy activo"),
        description = "Esto ayudará a recomendar mascotas con energía compatible"
    ),
    QuizQuestion(
        id = "pet_size",
        question = "¿Qué tamaño de mascota prefieres?",
        options = listOf("Pequeño", "Mediano", "Grande", "Cualquiera"),
        description = "Considera tu espacio disponible"
    ),
    QuizQuestion(
        id = "experience",
        question = "¿Tienes experiencia previa con mascotas?",
        options = listOf("Sí, mucha", "Algo", "No, es mi primera", "He tenido mascotas"),
        description = "Esto nos ayudará a recomendar mascotas apropiadas para tu nivel"
    ),
    QuizQuestion(
        id = "living_space",
        question = "¿Dónde vives?",
        options = listOf("Apartamento pequeño", "Apartamento grande", "Casa con patio", "Finca/Terreno"),
        description = "El espacio es importante para la salud de la mascota"
    ),
    QuizQuestion(
        id = "time_available",
        question = "¿Cuánto tiempo tienes disponible para una mascota?",
        options = listOf("Poco (menos de 1h/día)", "Moderado (1-2h/día)", "Bastante (2-4h/día)", "Mucho (4+ horas)"),
        description = "Algunas mascotas requieren más cuidado y atención"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIQuizScreen(
    navController: NavController,
    viewModel: AiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = QUIZ_QUESTIONS.size
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
                            text = "Pregunta $answeredQuestions de $currentQuestion",
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (uiState.showRecommendations) {
            AIResultsScreen(
                recommendations = uiState.recommendations,
                navController = navController,
                quizAnswers = uiState.quizAnswers,
                onRestart = {
                    viewModel.resetQuiz()
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Barra de progreso
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((answeredQuestions.toFloat() / QUIZ_QUESTIONS.size))
                                .height(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    // Mostrar la primera pregunta sin responder
                    val unansweredQuestion = QUIZ_QUESTIONS.firstOrNull { !uiState.quizAnswers.containsKey(it.id) }
                    
                    if (unansweredQuestion != null) {
                        QuestionCard(
                            question = unansweredQuestion,
                            selectedAnswer = uiState.quizAnswers[unansweredQuestion.id],
                            onAnswerSelected = { answer ->
                                viewModel.updateQuizAnswer(unansweredQuestion.id, answer)
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    
                    // Botón para continuar o enviar
                    val isAllAnswered = uiState.quizAnswers.size == QUIZ_QUESTIONS.size
                    
                    Button(
                        onClick = {
                            if (isAllAnswered) {
                                viewModel.submitQuiz()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isLoading && (answeredQuestions > 0),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAllAnswered) "Ver recomendaciones" else "Siguiente",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }

                // Mostrar respuestas anteriores para contexto
                if (answeredQuestions > 0) {
                    item {
                        Text(
                            text = "Tus respuestas",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    items(answeredQuestions) { index ->
                        val q = QUIZ_QUESTIONS[index]
                        val answer = uiState.quizAnswers[q.id] ?: ""
                        AnswerSummary(question = q.question, answer = answer)
                    }
                }
            }
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

            // Opciones como radio buttons
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
