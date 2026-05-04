package com.pethelp.app.features.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiMessage
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiUiState(
    val isLoading: Boolean = false,
    val aiResponse: String = "",
    val error: String? = null,
    val conversationHistory: List<AiMessage> = emptyList(),
    val quizAnswers: Map<String, String> = emptyMap(),
    val recommendations: String = "",
    val showRecommendations: Boolean = false
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    fun updateQuizAnswer(question: String, answer: String) {
        val currentAnswers = _uiState.value.quizAnswers.toMutableMap()
        currentAnswers[question] = answer
        _uiState.value = _uiState.value.copy(quizAnswers = currentAnswers)
    }

    fun submitQuiz() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = aiRepository.getRecommendedPetsBasedOnQuiz(
                _uiState.value.quizAnswers
            )

            result.onSuccess { recommendations ->
                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations,
                    showRecommendations = true,
                    isLoading = false
                )
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error desconocido",
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Agregar mensaje del usuario al historial
            val updatedHistory = _uiState.value.conversationHistory.toMutableList()
            updatedHistory.add(AiMessage(role = "user", content = userMessage))

            // Llamar a OpenRouter
            val request = AiChatRequest(
                messages = updatedHistory
            )

            val result = aiRepository.callOpenRouter(request)

            result.onSuccess { response ->
                val assistantMessage = response.choices?.firstOrNull()?.message
                if (assistantMessage != null) {
                    updatedHistory.add(assistantMessage)
                    _uiState.value = _uiState.value.copy(
                        conversationHistory = updatedHistory,
                        aiResponse = assistantMessage.content,
                        isLoading = false
                    )
                }
            }

            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Error en la llamada a OpenRouter",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetQuiz() {
        _uiState.value = AiUiState()
    }
}
