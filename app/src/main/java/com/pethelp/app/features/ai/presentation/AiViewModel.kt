/**
 * ViewModel del asistente de IA y el quiz de recomendación de mascotas.
 *
 * Gestiona el estado del quiz, envía respuestas al modelo de lenguaje
 * (Gemini/NVIDIA) y carga publicaciones reales de Firestore que coincidan
 * con el tipo de mascota recomendado.
 */
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

/**
 * Estado inmutable que representa la UI del asistente de IA.
 *
 * Agrupa el estado del quiz de recomendación, la conversación de chat,
 * las publicaciones recomendadas y los indicadores de carga/error.
 *
 * @param isLoading Indica si hay una operación de IA en curso (quiz o chat).
 * @param aiResponse Última respuesta recibida del modelo de lenguaje.
 * @param error Mensaje de error si la llamada a IA falla.
 * @param conversationHistory Historial de mensajes del chat con la IA.
 * @param quizAnswers Mapa de preguntas del quiz a respuestas seleccionadas.
 * @param currentQuestionIndex Índice de la pregunta actual en el quiz.
 * @param recommendations Texto de recomendaciones generado por la IA tras completar el quiz.
 * @param showRecommendations Indica si se debe mostrar la pantalla de resultados.
 * @param recommendedPosts Lista de publicaciones filtradas según el tipo de mascota del quiz.
 * @param isLoadingRecommendedPosts Indica si se están cargando posts recomendados desde Firestore.
 */
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

/**
 * ViewModel del asistente de IA y el quiz de recomendación de mascotas.
 *
 * **Responsabilidad Principal:**
 * - Gestionar el estado del quiz de adopción (respuestas, navegación entre preguntas).
 * - Enviar las respuestas del quiz al [AiChatRepository] para obtener recomendaciones.
 * - Mantener una conversación de chat con el modelo de lenguaje (Gemini/NVIDIA).
 * - Cargar publicaciones reales de Firestore que coincidan con el tipo de mascota recomendado.
 *
 * **Arquitectura:**
 * - Usa [MutableStateFlow] para el estado reactivo de la UI.
 * - Delega las llamadas de red al [AiChatRepository] (IA) y [PostRepository] (Firestore).
 * - Cancela trabajos previos de carga de posts para evitar resultados desactualizados.
 *
 * @param aiRepository Repositorio que comunica con Gemini/NVIDIA para IA.
 * @param postRepository Repositorio de publicaciones para cargar posts recomendados.
 */
@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiChatRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    /** Estado mutable interno; expuesto como solo lectura a través de [uiState]. */
    private val _uiState = MutableStateFlow(AiUiState())

    /** Estado público observable por los composables de IA. */
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    /** Job activo para la carga de posts recomendados; permite cancelación. */
    private var recommendedPostsJob: Job? = null

    /**
     * Registra o actualiza la respuesta de una pregunta del quiz.
     *
     * @param question Texto o clave de la pregunta.
     * @param answer Respuesta seleccionada por el usuario.
     */
    fun updateQuizAnswer(question: String, answer: String) {
        val currentAnswers = _uiState.value.quizAnswers.toMutableMap()
        currentAnswers[question] = answer
        _uiState.value = _uiState.value.copy(quizAnswers = currentAnswers)
    }

    /**
     * Avanza a la siguiente pregunta del quiz.
     *
     * @param totalQuestions Cantidad total de preguntas en el quiz.
     */
    fun goToNextQuestion(totalQuestions: Int) {
        val nextIndex = (_uiState.value.currentQuestionIndex + 1)
            .coerceAtMost((totalQuestions - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(currentQuestionIndex = nextIndex)
    }

    /** Retrocede a la pregunta anterior del quiz. */
    fun goToPreviousQuestion() {
        val previousIndex = (_uiState.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentQuestionIndex = previousIndex)
    }

    /**
     * Envía las respuestas del quiz al servicio de IA y luego carga posts recomendados.
     *
     * Si la llamada a IA tiene éxito, actualiza [recommendations] e inicia la observación
     * de publicaciones verificadas que coincidan con el tipo de mascota recomendado.
     */
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

    /**
     * Envía un mensaje de chat al modelo de lenguaje y actualiza el historial.
     *
     * @param userMessage Texto escrito por el usuario en el chat.
     */
    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Agrega el mensaje del usuario al historial de conversación.
            val updatedHistory = _uiState.value.conversationHistory.toMutableList()
            updatedHistory.add(AiMessage(role = "user", content = userMessage))

            // Construye la petición con todo el historial para contexto.
            val request = AiChatRequest(
                messages = updatedHistory
            )

            // Llama al repositorio de IA (Gemini primero, NVIDIA fallback).
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

    /** Limpia el mensaje de error actual del estado de la UI. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reinicia el quiz y cancela cualquier observación de posts recomendados.
     *
     * Útil cuando el usuario quiere volver a hacer el quiz desde cero.
     */
    fun resetQuiz() {
        recommendedPostsJob?.cancel()
        _uiState.value = AiUiState()
    }

    /**
     * Observa publicaciones verificadas y filtra las compatibles con el tipo de mascota del quiz.
     *
     * Cancela el job anterior para evitar resultados desactualizados si el usuario
     * cambia rápidamente de respuestas. Solo carga posts [PostStatus.VERIFIED]
     * para mantener consistencia con el feed principal.
     *
     * @param answers Mapa de respuestas del quiz; se espera clave "pet_type".
     */
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

    /**
     * Compara el tipo de mascota de un post con el seleccionado en el quiz.
     *
     * Usa [normalizePetType] para manejar equivalencias en español e inglés.
     *
     * @param post Publicación a evaluar.
     * @param selectedPetType Tipo de mascota elegido por el usuario.
     * @return true si coinciden (o si ambos son "other").
     */
    private fun matchesSelectedPetType(post: Post, selectedPetType: String): Boolean {
        val normalizedSelected = normalizePetType(selectedPetType)
        val normalizedPost = normalizePetType(post.animalType)
        return when (normalizedSelected) {
            "other" -> normalizedPost == "other"
            else -> normalizedPost == normalizedSelected
        }
    }

    /**
     * Normaliza un tipo de mascota a una clave estándar en inglés.
     *
     * Soporta entrada en español ("perro", "gato", etc.) e inglés
     * para que el quiz sea robusto ante diferentes idiomas.
     *
     * @param value Texto bruto del tipo de mascota.
     * @return Clave normalizada: "dog", "cat", "rabbit", "bird" o "other".
     */
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
