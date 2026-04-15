package com.pethelp.app.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val allPublicPosts: List<Post> = emptyList(),
    val selectedCategory: PostCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredPosts: List<Post>
        get() = if (selectedCategory == null) {
            allPublicPosts
        } else {
            allPublicPosts.filter { it.category == selectedCategory }
        }
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadPublicPosts()
    }

    fun loadPublicPosts() {
        viewModelScope.launch {
            postRepository.getPosts(category = null).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        val publicPosts = (resource.data ?: emptyList())
                            .filter { it.status == PostStatus.VERIFIED }
                            .sortedByDescending { it.createdAt }

                        _uiState.update {
                            it.copy(
                                allPublicPosts = publicPosts,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "No fue posible cargar las publicaciones."
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectCategory(category: PostCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
