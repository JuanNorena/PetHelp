/**
 * ViewModel del panel de moderación.
 *
 * Gestiona métricas de moderación, publicaciones pendientes de revisión
 * y acciones de aprobar/rechazar posts con análisis asistido por IA.
 */
package com.pethelp.app.features.moderation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.ModerationAiAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Representa las métricas de gestión del panel de moderación.
 *
 * @property pendingCount Número de publicaciones esperando revisión.
 * @property approvedToday Cantidad de publicaciones verificadas en la sesión actual.
 * @property rejectedToday Cantidad de publicaciones rechazadas en la sesión actual.
 * @property approvalRate Porcentaje de éxito (aprobaciones vs total gestionado).
 * @property totalUsers Contador total de usuarios registrados en la plataforma.
 * @property totalAdoptions Contador histórico de mascotas que encontraron hogar.
 * @property activeReports Número de reportes de usuarios o contenido marcados como sospechosos.
 */
data class ModerationStats(
    val pendingCount: Int = 0,
    val approvedToday: Int = 0,
    val rejectedToday: Int = 0,
    val approvalRate: Int = 0,
    val totalUsers: Int = 0,
    val totalAdoptions: Int = 0,
    val activeReports: Int = 0
)

/**
 * Estado inmutable de la interfaz de usuario para el flujo de moderación.
 *
 * @property pendingPosts Lista de publicaciones en estado PENDING.
 * @property moderatedPostsToday Historial de publicaciones gestionadas en el día.
 * @property stats Objeto con métricas y contadores para el dashboard.
 * @property selectedPost Publicación cargada para inspección detallada.
 * @property isLoading Indica carga inicial de datos.
 * @property isActionLoading Indica que se está procesando una decisión (Aprobar/Rechazar).
 * @property error Mensaje de error localizado.
 * @property reportedUsers Lista de usuarios bajo investigación por reportes.
 */
data class ModerationUiState(
    val pendingPosts: List<Post> = emptyList(),
    val moderatedPostsToday: List<Post> = emptyList(),
    val stats: ModerationStats = ModerationStats(),
    val selectedPost: Post? = null,
    val aiAnalysis: ModerationAiAnalysis? = null,
    val isAiAnalysisLoading: Boolean = false,
    val aiAnalysisError: UiText? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: UiText? = null,
    val reportedUsers: List<com.pethelp.app.core.domain.model.User> = emptyList()
)

/**
 * ViewModel encargado de orquestar la lógica de moderación de contenido.
 *
 * **Responsabilidad Principal:**
 * Gestionar las solicitudes de aprobación o rechazo de publicaciones, manteniendo un control
 * estricto sobre el flujo de datos y estadísticas globales del sistema PetHelp.
 *
 * **Arquitectura:**
 * Sigue el patrón MVVM y utiliza Clean Architecture mediante la inyección del [PostRepository].
 * Implementa flujos de eventos asíncronos para notificaciones visuales (Snackbars) y navegación.
 *
 * **Notas para Junior Developers:**
 * - Se utilizan [Job]s para gestionar y cancelar corrutinas activas, evitando colisiones de datos.
 * - [MutableSharedFlow] se emplea para eventos de "un solo uso" (one-shot) que no deben persistir
 *   tras rotar la pantalla.
 * - La función `executeModerationAction` centraliza la lógica repetitiva de manejo de estados,
 *   reduciendo la duplicación de código.
 *
 * @property postRepository Interfaz para interactuar con la persistencia de posts y métricas.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */
@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val aiChatRepository: AiChatRepository
) : ViewModel() {

    // ── Estados de la UI ────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(ModerationUiState())
    /** Estado público reactivo observado por las pantallas de moderación. */
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    /** Canal para emitir mensajes temporales de éxito o error. */
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    private val _actionCompleted = MutableSharedFlow<Unit>()
    /** Notifica a la UI que una acción de moderación terminó exitosamente para navegar atrás. */
    val actionCompleted: SharedFlow<Unit> = _actionCompleted.asSharedFlow()

    // Gestión de trabajos de corrutinas para evitar múltiples peticiones simultáneas.
    private var pendingPostsJob: Job? = null
    private var moderatedPostsJob: Job? = null
    private var postDetailJob: Job? = null
    private var aiAnalysisJob: Job? = null

    init {
        // Al iniciar, cargamos toda la información necesaria para el Dashboard.
        loadDashboardData()
    }

    // ── Carga de Datos y Métricas ───────────────────────────────────────────
    /**
     * Orquesta la carga simultánea de posts pendientes, historial del día y métricas globales.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            loadPendingPosts(forceRefresh = true)
            loadModeratedPostsToday()
            loadGlobalMetrics()
        }
    }

    /**
     * Recupera indicadores globales de la base de datos (Total usuarios, adoptions, etc.).
     */
    private fun loadGlobalMetrics() {
        viewModelScope.launch {
            postRepository.getGlobalMetrics().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val metrics = resource.data
                        _uiState.value = _uiState.value.let { state ->
                            state.copy(
                                stats = state.stats.copy(
                                    totalUsers = metrics?.get("totalUsers") as? Int ?: 0,
                                    totalAdoptions = metrics?.get("totalAdoptions") as? Int ?: 0,
                                    activeReports = metrics?.get("activeReports") as? Int ?: 0
                                )
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Carga la lista de posts que ya han sido gestionados durante el día de hoy.
     */
    fun loadModeratedPostsToday() {
        moderatedPostsJob?.cancel()
        moderatedPostsJob = viewModelScope.launch {
            postRepository.getModeratedPostsToday().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val posts = resource.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            moderatedPostsToday = posts,
                            stats = calculateStats(_uiState.value.pendingPosts.size, posts)
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Calcula dinámicamente el resumen de estadísticas locales para el dashboard.
     * @return Objeto [ModerationStats] con los cálculos actualizados.
     */
    private fun calculateStats(pendingCount: Int, moderatedToday: List<Post>): ModerationStats {
        val approved = moderatedToday.count { it.status == PostStatus.VERIFIED }
        val rejected = moderatedToday.count { it.status == PostStatus.REJECTED }
        val total = approved + rejected
        val rate = if (total > 0) (approved * 100) / total else 0
        
        return ModerationStats(
            pendingCount = pendingCount,
            approvedToday = approved,
            rejectedToday = rejected,
            approvalRate = rate
        )
    }

    /**
     * Recupera de forma asíncrona los posts en espera de revisión.
     * @param forceRefresh Si es true, cancela cualquier carga previa y reinicia la petición.
     */
    fun loadPendingPosts(forceRefresh: Boolean = false) {
        // Evitamos peticiones duplicadas si ya hay una carga en curso.
        if (pendingPostsJob != null && !forceRefresh) return

        pendingPostsJob?.cancel()
        pendingPostsJob = viewModelScope.launch {
            postRepository.getPendingPosts().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }

                    is Resource.Success -> {
                        val posts = resource.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            pendingPosts = posts,
                            stats = calculateStats(posts.size, _uiState.value.moderatedPostsToday),
                            isLoading = false,
                            error = null
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.uiText
                        )
                    }
                }
            }
        }
    }

    /**
     * Obtiene la información completa de un post para su inspección detallada.
     * @param postId Identificador único de la publicación.
     */
    fun loadPostDetail(postId: String) {
        postDetailJob?.cancel()
        postDetailJob = viewModelScope.launch {
            postRepository.getPostById(postId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null,
                            aiAnalysis = null,
                            aiAnalysisError = null,
                            isAiAnalysisLoading = false
                        )
                    }

                    is Resource.Success -> {
                        val post = resource.data
                        _uiState.value = _uiState.value.copy(
                            selectedPost = post,
                            isLoading = false,
                            error = null
                        )
                        if (post != null) {
                            loadAiAnalysis(post)
                        }
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.uiText
                        )
                    }
                }
            }
        }
    }

    private fun loadAiAnalysis(post: Post) {
        aiAnalysisJob?.cancel()
        aiAnalysisJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiAnalysisLoading = true,
                aiAnalysisError = null,
                aiAnalysis = null
            )

            val result = aiChatRepository.analyzePostForModeration(post)
            result.onSuccess { analysis ->
                _uiState.value = _uiState.value.copy(
                    aiAnalysis = analysis,
                    isAiAnalysisLoading = false,
                    aiAnalysisError = null
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isAiAnalysisLoading = false,
                    aiAnalysisError = UiText.DynamicString(
                        exception.message ?: "No fue posible generar el analisis de IA."
                    )
                )
            }
        }
    }

    // ── Acciones de Moderación ──────────────────────────────────────────────
    /**
     * Aprueba una publicación, permitiendo que sea visible en el Feed global.
     */
    fun approvePost(postId: String) {
        executeModerationAction(
            action = { postRepository.approvePost(postId) },
            successMessage = UiText.StringResource(R.string.moderation_post_approved_success),
            moderatedPostId = postId
        )
    }

    /**
     * Rechaza una publicación por no cumplir con las normas de la comunidad.
     * @param reason Motivo obligatorio del rechazo que se enviará al autor.
     */
    fun rejectPost(postId: String, reason: String) {
        val normalizedReason = reason.trim()
        
        // Validación local para evitar peticiones con motivos vacíos.
        if (normalizedReason.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit(UiText.StringResource(R.string.moderation_reject_reason_required))
            }
            return
        }

        executeModerationAction(
            action = { postRepository.rejectPost(postId, normalizedReason) },
            successMessage = UiText.StringResource(R.string.moderation_post_rejected_success),
            moderatedPostId = postId
        )
    }

    /**
     * Limpia cualquier mensaje de error activo en la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Función helper centralizada para ejecutar cualquier acción de moderación.
     *
     * **PASO 1:** Cambia el estado a `isActionLoading`.
     * **PASO 2:** Ejecuta la acción del repositorio (Aprobar o Rechazar).
     * **PASO 3:** Al tener éxito, actualiza las listas locales eliminando el post gestionado.
     * **PASO 4:** Dispara notificaciones visuales y recarga métricas.
     *
     * @param action Lambda que retorna un Flow del repositorio.
     * @param successMessage Texto a mostrar si la acción tiene éxito.
     * @param moderatedPostId ID del post para actualizar la UI localmente.
     */
    private fun executeModerationAction(
        action: () -> kotlinx.coroutines.flow.Flow<Resource<Unit>>,
        successMessage: UiText,
        moderatedPostId: String? = null
    ) {
        viewModelScope.launch {
            action().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isActionLoading = true, error = null)
                    }

                    is Resource.Success -> {
                        _uiState.value = _uiState.value.let { state ->
                            // Optimización: Eliminamos el post de la lista sin esperar a la recarga completa.
                            val updatedPendingPosts = if (moderatedPostId == null) {
                                state.pendingPosts
                            } else {
                                state.pendingPosts.filterNot { it.id == moderatedPostId }
                            }

                            val updatedSelectedPost = if (state.selectedPost?.id == moderatedPostId) {
                                null
                            } else {
                                state.selectedPost
                            }

                            state.copy(
                                isActionLoading = false,
                                pendingPosts = updatedPendingPosts,
                                selectedPost = updatedSelectedPost
                            )
                        }
                        
                        // Notificaciones y recarga de contadores.
                        _snackbarMessage.emit(successMessage)
                        _actionCompleted.emit(Unit)
                        loadPendingPosts(forceRefresh = true)
                        loadModeratedPostsToday()
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isActionLoading = false,
                            error = resource.uiText
                        )
                        _snackbarMessage.emit(resource.uiText ?: UiText.StringResource(R.string.moderation_action_error))
                    }
                }
            }
        }
    }
}
