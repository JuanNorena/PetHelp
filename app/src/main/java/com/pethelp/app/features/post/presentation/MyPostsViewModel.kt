package com.pethelp.app.features.post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MyPostsTab(val label: String) {
    ACTIVE("Activas"),
    IN_REVIEW("En revisión"),
    RESOLVED("Finalizadas")
}

data class MyPostsUiState(
    val allPosts: List<Post> = emptyList(),
    val selectedTab: MyPostsTab = MyPostsTab.ACTIVE,
    val isLoading: Boolean = true,
    val error: String? = null,
    val postPendingDelete: Post? = null,
    val isDeleting: Boolean = false
)

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val filteredPosts: StateFlow<List<Post>> = uiState
        .map { state ->
            state.allPosts.filter { post ->
                when (state.selectedTab) {
                    MyPostsTab.ACTIVE    -> post.status == PostStatus.VERIFIED
                    MyPostsTab.IN_REVIEW -> post.status == PostStatus.PENDING
                                        || post.status == PostStatus.REJECTED
                    MyPostsTab.RESOLVED  -> post.status == PostStatus.RESOLVED
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadMyPosts()
    }

    private fun loadMyPosts() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            postRepository.getPostsByUser(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            allPosts = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }

    fun selectTab(tab: MyPostsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun requestDelete(post: Post) {
        _uiState.update { it.copy(postPendingDelete = post) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(postPendingDelete = null) }
    }

    fun confirmDelete() {
        val post = _uiState.value.postPendingDelete ?: return
        _uiState.update { it.copy(postPendingDelete = null, isDeleting = true) }
        viewModelScope.launch {
            postRepository.deletePost(post.id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> { /* isDeleting ya fue puesto en true */ }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false) }
                        _snackbarMessage.emit("Publicación eliminada.")
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isDeleting = false) }
                        _snackbarMessage.emit(resource.message ?: "Error al eliminar.")
                    }
                }
            }
        }
    }

    fun pausePost(postId: String) {
        viewModelScope.launch {
            postRepository.togglePostStatus(postId, isPaused = true).collect { resource ->
                when (resource) {
                    is Resource.Success -> _snackbarMessage.emit("Publicación pausada y enviada a revisión.")
                    is Resource.Error   -> _snackbarMessage.emit(resource.message ?: "Error al pausar.")
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }

    fun markAsResolved(postId: String) {
        viewModelScope.launch {
            postRepository.markAsResolved(postId).collect { resource ->
                when (resource) {
                    is Resource.Success -> _snackbarMessage.emit("¡Publicación marcada como resuelta!")
                    is Resource.Error   -> _snackbarMessage.emit(resource.message ?: "Error al marcar como resuelta.")
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }
}
