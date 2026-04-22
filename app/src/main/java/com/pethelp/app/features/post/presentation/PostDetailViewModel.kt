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
    }

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

    private fun loadComments() {
        viewModelScope.launch {
            postRepository.getComments(postId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(
                        comments = resource.data ?: emptyList()
                    )
                }
            }
        }
    }

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
            val comment = Comment(
                postId = postId,
                authorId = userId,
                authorName = userDoc?.getString("name")
                    ?.takeIf { it.isNotBlank() }
                    ?: currentUserName.ifBlank { "Usuario" },
                authorPhotoUrl = userDoc?.getString("photoUrl").orEmpty(),
                text = text.trim()
            )

            postRepository.addComment(comment).collect { resource ->
                when (resource) {
                    is Resource.Error -> _snackbarMessage.emit(
                        resource.message ?: UiText.StringResource(R.string.comment_error)
                    )
                    else -> { /* success handled by live listener */ }
                }
            }
        }
    }

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
