package com.pethelp.app.features.post.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Comment
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val hasVoted: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val currentUserId: String get() = firebaseAuth.currentUser?.uid ?: ""
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

    fun toggleVote() {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit("Inicia sesión para votar.") }
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
                    is Resource.Error -> _snackbarMessage.emit(resource.message ?: "Error al votar.")
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun addComment(text: String) {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit("Inicia sesión para comentar.") }
            return
        }
        if (text.isBlank()) return

        val comment = Comment(
            postId = postId,
            authorId = userId,
            authorName = currentUserName.ifBlank { "Usuario" },
            text = text.trim()
        )

        viewModelScope.launch {
            postRepository.addComment(comment).collect { resource ->
                when (resource) {
                    is Resource.Error -> _snackbarMessage.emit(resource.message ?: "Error al comentar.")
                    else -> { /* success handled by live listener */ }
                }
            }
        }
    }

    fun requestAdoption(message: String) {
        val userId = currentUserId
        if (userId.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit("Inicia sesión para solicitar adopción.") }
            return
        }
        viewModelScope.launch {
            postRepository.requestAdoption(postId, userId, message).collect { resource ->
                when (resource) {
                    is Resource.Success -> _snackbarMessage.emit("¡Solicitud de adopción enviada!")
                    is Resource.Error -> _snackbarMessage.emit(resource.message ?: "Error al enviar solicitud.")
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }
}
