package com.pethelp.app.features.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiMessage
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val currentQuestionIndex: Int = 0,
    val recommendations: String = "",
    val showRecommendations: Boolean = false,
    val recommendedPosts: List<Post> = emptyList(),
    val isLoadingRecommendedPosts: Boolean = false
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiChatRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()
    private var recommendedPostsJob: Job? = null

    fun updateQuizAnswer(question: String, answer: String) {
        val currentAnswers = _uiState.value.quizAnswers.toMutableMap()
        currentAnswers[question] = answer
        _uiState.value = _uiState.value.copy(quizAnswers = currentAnswers)
    }

    fun goToNextQuestion(totalQuestions: Int) {
        val nextIndex = (_uiState.value.currentQuestionIndex + 1)
            .coerceAtMost((totalQuestions - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(currentQuestionIndex = nextIndex)
    }

    fun goToPreviousQuestion() {
        val previousIndex = (_uiState.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentQuestionIndex = previousIndex)
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
                observeRecommendedPosts(_uiState.value.quizAnswers)
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

            // Llamar a Gemini via Firebase AI Logic
            val request = AiChatRequest(
                messages = updatedHistory
            )

            val result = aiRepository.callGemini(request)

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
                    error = exception.message ?: "Error en la llamada a Gemini",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetQuiz() {
        recommendedPostsJob?.cancel()
        _uiState.value = AiUiState()
    }

    private fun observeRecommendedPosts(answers: Map<String, String>) {
        recommendedPostsJob?.cancel()
        val selectedPetType = answers["pet_type"].orEmpty()
        if (selectedPetType.isBlank()) {
            _uiState.value = _uiState.value.copy(
                recommendedPosts = emptyList(),
                isLoadingRecommendedPosts = false
            )
            return
        }

        recommendedPostsJob = viewModelScope.launch {
            postRepository.getPosts(category = null).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoadingRecommendedPosts = true)
                    }
                    is Resource.Success -> {
                        val compatiblePosts = (resource.data ?: emptyList())
                            .filter { it.status == PostStatus.VERIFIED }
                            .filter { matchesSelectedPetType(it, selectedPetType) }
                            .sortedByDescending { it.createdAt }
                            .take(6)

                        _uiState.value = _uiState.value.copy(
                            recommendedPosts = compatiblePosts,
                            isLoadingRecommendedPosts = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            recommendedPosts = emptyList(),
                            isLoadingRecommendedPosts = false
                        )
                    }
                }
            }
        }
    }

    private fun matchesSelectedPetType(post: Post, selectedPetType: String): Boolean {
        val normalizedSelected = normalizePetType(selectedPetType)
        val normalizedPost = normalizePetType(post.animalType)
        return when (normalizedSelected) {
            "other" -> normalizedPost == "other"
            else -> normalizedPost == normalizedSelected
        }
    }

    private fun normalizePetType(value: String): String {
        val text = value.trim().lowercase()
        return when {
            text.contains("perro") || text.contains("dog") -> "dog"
            text.contains("gato") || text.contains("cat") -> "cat"
            text.contains("conejo") || text.contains("rabbit") -> "rabbit"
            text.contains("ave") || text.contains("pajaro") || text.contains("pájaro") || text.contains("bird") -> "bird"
            else -> "other"
        }
    }
}
