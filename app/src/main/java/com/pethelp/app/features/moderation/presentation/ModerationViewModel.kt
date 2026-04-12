package com.pethelp.app.features.moderation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
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

data class ModerationUiState(
    val pendingPosts: List<Post> = emptyList(),
    val selectedPost: Post? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _actionCompleted = MutableSharedFlow<Unit>()
    val actionCompleted: SharedFlow<Unit> = _actionCompleted.asSharedFlow()

    private var pendingPostsJob: Job? = null
    private var postDetailJob: Job? = null

    fun loadPendingPosts(forceRefresh: Boolean = false) {
        if (pendingPostsJob != null && !forceRefresh) return

        pendingPostsJob?.cancel()
        pendingPostsJob = viewModelScope.launch {
            postRepository.getPendingPosts().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }

                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            pendingPosts = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun loadPostDetail(postId: String) {
        postDetailJob?.cancel()
        postDetailJob = viewModelScope.launch {
            postRepository.getPostById(postId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }

                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            selectedPost = resource.data,
                            isLoading = false,
                            error = null
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun approvePost(postId: String) {
        executeModerationAction(
            action = { postRepository.approvePost(postId) },
            successMessage = "Publicación aprobada correctamente."
        )
    }

    fun rejectPost(postId: String, reason: String) {
        val normalizedReason = reason.trim()
        if (normalizedReason.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Debes ingresar un motivo para rechazar.")
            }
            return
        }

        executeModerationAction(
            action = { postRepository.rejectPost(postId, normalizedReason) },
            successMessage = "Publicación rechazada correctamente."
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun executeModerationAction(
        action: () -> kotlinx.coroutines.flow.Flow<Resource<Unit>>,
        successMessage: String
    ) {
        viewModelScope.launch {
            action().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isActionLoading = true, error = null)
                    }

                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(isActionLoading = false)
                        _snackbarMessage.emit(successMessage)
                        _actionCompleted.emit(Unit)
                        loadPendingPosts(forceRefresh = true)
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isActionLoading = false,
                            error = resource.message
                        )
                        _snackbarMessage.emit(resource.message ?: "No fue posible completar la acción.")
                    }
                }
            }
        }
    }
}
