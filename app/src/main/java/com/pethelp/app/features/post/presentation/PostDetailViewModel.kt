/**
 * ViewModel del detalle de una publicación.
 *
 * Sincroniza datos del post, comentarios, votos, favoritos y estado de
 * solicitud de adopción. Expone acciones de interacción como votar,
 * comentar y solicitar adopción.
 */
package com.pethelp.app.features.post.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import com.pethelp.app.features.post.domain.repository.PostRepository
import com.pethelp.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estado de la pantalla de detalle de publicacion.
 */
data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val hasVoted: Boolean = false,
    val isFavorite: Boolean = false,
    val existingAdoptionRequest: AdoptionRequest? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null
)

@HiltViewModel
/**
 * ViewModel para el detalle de una publicacion.
 *
 * Sincroniza informacion del post, comentarios y estado de voto, y expone acciones
 * de interaccion como votar, comentar y solicitar adopcion.
 */
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val _uiState = MutableStateFlow(PostDetailUiState())

    /** Estado de solo lectura observado por la UI. */
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()

    /** Eventos de feedback rapido para mensajes en pantalla. */
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    /** Identificador del usuario actual autenticado. */
    val currentUserId: String get() = firebaseAuth.currentUser?.uid ?: ""

    /** Nombre visible del usuario actual para prellenar comentarios. */
    val currentUserName: String get() = firebaseAuth.currentUser?.displayName ?: ""

    init {
        loadPost()
        loadComments()
        checkVoteStatus()
        checkFavoriteStatus()
        checkAdoptionRequestStatus()
    }

    // ── Carga de Datos del Post ─────────────────────────────────────────────
    /**
     * Escucha en tiempo real los cambios del post desde Firestore.
     *
     * Usa [PostRepository.getPostById] con snapshot listener para mantener
     * la UI sincronizada si otro usuario modifica la publicación.
     */
    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPostById(postId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        post = resource.data,
                        isLoading = false,
                        error = null
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = resource.message
                    )
                }
            }
        }
    }

    /**
     * Escucha comentarios en tiempo real y fusiona con comentarios temporales locales.
     *
     * **Lógica de merge:**
     * 1. Filtra comentarios locales temporales (prefijo "temp_").
     * 2. Identifica temporales que ya llegaron a Firestore comparando autor, texto y timestamp cercano.
     * 3. Elimina duplicados por clave compuesta y ordena cronológicamente.
     *
     * Esto permite mostrar inmediatamente un comentario enviado (optimista)
     * mientras se sincroniza con Firestore.
     */
    private fun loadComments() {
        viewModelScope.launch {
            postRepository.getComments(postId).collect { resource ->
                if (resource is Resource.Success) {
                    val incoming = resource.data ?: emptyList()
                    val current = _uiState.value.comments
                    // Comentarios locales enviados pero aún no confirmados por Firestore.
                    val temp = current.filter { it.id.startsWith("temp_") }

                    // Identifica temporales que ya existen en Firestore (mismo autor, texto y tiempo cercano).
                    val replacedTempIds = temp.filter { tempComment ->
                        incoming.any { real ->
                            real.authorId == tempComment.authorId &&
                                real.text == tempComment.text &&
                                real.createdAt in (tempComment.createdAt - 100)..(tempComment.createdAt + 2000)
                        }
                    }.map { it.id }

                    // Conserva temporales no confirmados aún y añade los reales de Firestore.
                    val merged = incoming + temp.filter { it.id !in replacedTempIds }
                    val deduped = merged
                        .distinctBy { "${it.authorId}_${it.createdAt}_${it.text}" }
                        .sortedBy { it.createdAt }

                    _uiState.value = _uiState.value.copy(comments = deduped)
                }
            }
        }
    }

    /**
     * Consulta si el usuario autenticado ya votó (like) esta publicación.
     */
    private fun checkVoteStatus() {
        val userId = currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch {
            postRepository.hasUserVoted(postId, userId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(hasVoted = resource.data ?: false)
                }
            }
        }
    }

    /**
     * Consulta si el usuario autenticado tiene esta publicación como favorita.
     *
     * En esta implementación, favorito se almacena en la misma colección de votos.
     */
    private fun checkFavoriteStatus() {
        val userId = currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch {
            postRepository.hasUserVoted(postId, userId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(isFavorite = resource.data ?: false)
                }
            }
        }
    }

    /**
     * Verifica si el usuario ya envió una solicitud de adopción para este post.
     *
     * Actualiza [PostDetailUiState.existingAdoptionRequest] para que la UI
     * pueda mostrar el estado adecuado (ej. botón deshabilitado).
     */
    private fun checkAdoptionRequestStatus() {
        val userId = currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch {
            postRepository.getAdoptionRequestForUserAndPost(postId, userId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(existingAdoptionRequest = resource.data)
                }
            }
        }
    }

    /** Alterna el voto del usuario autenticado sobre la publicacion actual. */
    fun toggleVote() {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.StringResource(R.string.vote_login_required)) }
            return
        }
        viewModelScope.launch {
            val flow = if (_uiState.value.hasVoted) {
                postRepository.unvotePost(postId, userId)
            } else {
                postRepository.votePost(postId, userId)
            }
            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            hasVoted = !_uiState.value.hasVoted
                        )
                    }
                    is Resource.Error -> _snackbarMessage.emit(
                        resource.message ?: UiText.StringResource(R.string.vote_error)
                    )
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }

    /** Agrega un comentario nuevo al post actual. */
    fun addComment(text: String) {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.StringResource(R.string.comment_login_required)) }
            return
        }
        if (text.isBlank()) return

        viewModelScope.launch {
            val userDoc = getCurrentUserDoc(userId)
            val now = System.currentTimeMillis()
            val tempComment = Comment(
                id = "temp_${now}_${userId.hashCode()}",
                postId = postId,
                authorId = userId,
                authorName = userDoc?.getString("name")
                    ?.takeIf { it.isNotBlank() }
                    ?: currentUserName.ifBlank { "Usuario" },
                authorPhotoUrl = userDoc?.getString("photoUrl").orEmpty(),
                text = text.trim(),
                createdAt = now
            )

            // Actualizacion optimista: mostrar en UI de inmediato
            _uiState.value = _uiState.value.copy(
                comments = _uiState.value.comments + tempComment
            )

            postRepository.addComment(tempComment).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            comments = _uiState.value.comments.filter { it.id != tempComment.id }
                        )
                        _snackbarMessage.emit(
                            resource.message ?: UiText.StringResource(R.string.comment_error)
                        )
                    }
                    else -> { /* success handled by live listener */ }
                }
            }
        }
    }

    /** Alterna el estado de favorito del usuario autenticado sobre la publicacion actual. */
    fun toggleFavorite() {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.StringResource(R.string.vote_login_required)) }
            return
        }
        viewModelScope.launch {
            val isFavorite = _uiState.value.isFavorite
            postRepository.toggleFavorite(postId, userId, isFavorite).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val updatedVotes = resource.data ?: (_uiState.value.post?.votes ?: 0)
                        _uiState.value = _uiState.value.copy(
                            isFavorite = !isFavorite,
                            post = _uiState.value.post?.copy(votes = updatedVotes)
                        )
                    }
                    is Resource.Error -> _snackbarMessage.emit(
                        resource.message ?: UiText.StringResource(R.string.vote_error)
                    )
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }

    /**
     * Recupera el documento del usuario autenticado desde Firestore.
     *
     * @param userId UID del usuario.
     * @return DocumentSnapshot del usuario o null si falla la lectura.
     */
    private suspend fun getCurrentUserDoc(userId: String): DocumentSnapshot? {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
        } catch (_: Exception) {
            null
        }
    }

    /** Crea una solicitud de adopcion simplificada para el post actual. */
    fun requestAdoption(message: String) {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.StringResource(R.string.adoption_request_login_required)) }
            return
        }
        viewModelScope.launch {
            postRepository.requestAdoption(
                postId = postId,
                userId = userId,
                message = message,
                housingType = "No especificado",
                hasOutdoorSpace = "No especificado",
                hasExperience = "No especificado",
                phone = "No especificado",
                contactPreference = "Chat"
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> _snackbarMessage.emit(UiText.StringResource(R.string.adoption_request_success))
                    is Resource.Error -> _snackbarMessage.emit(
                        resource.message ?: UiText.StringResource(R.string.adoption_request_error)
                    )
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }
}
