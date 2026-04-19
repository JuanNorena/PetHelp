package com.pethelp.app.features.moderation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
import com.pethelp.app.core.domain.model.PostStatus
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

data class ModerationStats(
    val pendingCount: Int = 0,
    val approvedToday: Int = 0,
    val rejectedToday: Int = 0,
    val approvalRate: Int = 0,
    val totalUsers: Int = 0,
    val totalAdoptions: Int = 0,
    val activeReports: Int = 0
)

data class ModerationUiState(
    val pendingPosts: List<Post> = emptyList(),
    val moderatedPostsToday: List<Post> = emptyList(),
    val stats: ModerationStats = ModerationStats(),
    val selectedPost: Post? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: UiText? = null,
    val reportedUsers: List<com.pethelp.app.core.domain.model.User> = emptyList()
)

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    private val _actionCompleted = MutableSharedFlow<Unit>()
    val actionCompleted: SharedFlow<Unit> = _actionCompleted.asSharedFlow()

    private var pendingPostsJob: Job? = null
    private var moderatedPostsJob: Job? = null
    private var postDetailJob: Job? = null

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadPendingPosts()
            loadModeratedPostsToday()
            loadGlobalMetrics()
        }
    }

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
                            error = resource.uiText
                        )
                    }
                }
            }
        }
    }

    fun approvePost(postId: String) {
        executeModerationAction(
            action = { postRepository.approvePost(postId) },
            successMessage = UiText.StringResource(R.string.moderation_post_approved_success),
            moderatedPostId = postId
        )
    }

    fun rejectPost(postId: String, reason: String) {
        val normalizedReason = reason.trim()
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

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
