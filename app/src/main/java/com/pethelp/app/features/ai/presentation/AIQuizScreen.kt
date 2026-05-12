package com.pethelp.app.features.ai.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@Composable
private fun quizQuestions() = listOf(
    QuizQuestion(
        id = "pet_type",
        question = stringResource(R.string.ai_quiz_question_pet_type),
        options = listOf(
            stringResource(R.string.ai_quiz_option_dog),
            stringResource(R.string.ai_quiz_option_cat),
            stringResource(R.string.ai_quiz_option_rabbit),
            stringResource(R.string.ai_quiz_option_bird),
            stringResource(R.string.ai_quiz_option_other)
        ),
        description = stringResource(R.string.ai_quiz_desc_pet_type)
    ),
    QuizQuestion(
        id = "activity_level",
        question = stringResource(R.string.ai_quiz_question_activity),
        options = listOf(
            stringResource(R.string.ai_quiz_option_low),
            stringResource(R.string.ai_quiz_option_moderate),
            stringResource(R.string.ai_quiz_option_high),
            stringResource(R.string.ai_quiz_option_very_active)
        ),
        description = stringResource(R.string.ai_quiz_desc_activity)
    ),
    QuizQuestion(
        id = "pet_size",
        question = stringResource(R.string.ai_quiz_question_size),
        options = listOf(
            stringResource(R.string.ai_quiz_option_small),
            stringResource(R.string.ai_quiz_option_medium),
            stringResource(R.string.ai_quiz_option_large),
            stringResource(R.string.ai_quiz_option_any)
        ),
        description = stringResource(R.string.ai_quiz_desc_size)
    ),
    QuizQuestion(
        id = "experience",
        question = stringResource(R.string.ai_quiz_question_experience),
        options = listOf(
            stringResource(R.string.ai_quiz_option_lot_experience),
            stringResource(R.string.ai_quiz_option_some_experience),
            stringResource(R.string.ai_quiz_option_first_time),
            stringResource(R.string.ai_quiz_option_had_pets)
        ),
        description = stringResource(R.string.ai_quiz_desc_experience)
    ),
    QuizQuestion(
        id = "living_space",
        question = stringResource(R.string.ai_quiz_question_space),
        options = listOf(
            stringResource(R.string.ai_quiz_option_small_apartment),
            stringResource(R.string.ai_quiz_option_large_apartment),
            stringResource(R.string.ai_quiz_option_house_yard),
            stringResource(R.string.ai_quiz_option_farm)
        ),
        description = stringResource(R.string.ai_quiz_desc_space)
    ),
    QuizQuestion(
        id = "time_available",
        question = stringResource(R.string.ai_quiz_question_time),
        options = listOf(
            stringResource(R.string.ai_quiz_option_little_time),
            stringResource(R.string.ai_quiz_option_moderate_time),
            stringResource(R.string.ai_quiz_option_enough_time),
            stringResource(R.string.ai_quiz_option_lots_time)
        ),
        description = stringResource(R.string.ai_quiz_desc_time)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIQuizScreen(
    navController: NavController,
    viewModel: AiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val quizQuestions = quizQuestions()
    val totalQuestions = quizQuestions.size
    val currentQuestionIndex = uiState.currentQuestionIndex.coerceIn(0, totalQuestions - 1)
    val currentQuestion = quizQuestions[currentQuestionIndex]
    val selectedAnswer = uiState.quizAnswers[currentQuestion.id]
    val answeredQuestions = uiState.quizAnswers.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.ai_quiz_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = stringResource(R.string.ai_quiz_question_counter, currentQuestionIndex + 1, totalQuestions),
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
                    matchedPosts = uiState.recommendedPosts,
                    isLoadingPosts = uiState.isLoadingRecommendedPosts,
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
                        QuizIntroHeader(
                            currentQuestion = currentQuestionIndex + 1,
                            totalQuestions = totalQuestions
                        )
                    }

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
                                text = stringResource(R.string.ai_quiz_your_answers),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(quizQuestions.filter { uiState.quizAnswers.containsKey(it.id) }) { question ->
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
private fun QuizIntroHeader(currentQuestion: Int, totalQuestions: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.ai_quiz_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.ai_quiz_header_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.ai_quiz_question_counter, currentQuestion, totalQuestions),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuizProgressBar(progress: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
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
                    Text(stringResource(R.string.common_back), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                    text = if (isLastQuestion) stringResource(R.string.ai_quiz_view_recommendations) else stringResource(R.string.common_next),
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
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 3.dp
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
                val selected = selectedAnswer == option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnswerSelected(option) }
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(10.dp))
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
