package com.pethelp.app.features.post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val postsState = _postsState.asStateFlow()

    private val _actionState = MutableSharedFlow<Resource<Unit>>()
    val actionState = _actionState.asSharedFlow()

    init {
        loadMyPosts()
    }

    fun loadMyPosts() {
        authRepository.getCurrentUser().onEach { resource ->
            if (resource is Resource.Success && resource.data != null) {
                postRepository.getPostsByUser(resource.data.id).onEach { postsResource ->
                    _postsState.value = postsResource
                }.launchIn(viewModelScope)
            } else if (resource is Resource.Error) {
                _postsState.value = Resource.Error(resource.message ?: "Error al obtener usuario")
            }
        }.launchIn(viewModelScope)
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId).onEach { 
                _actionState.emit(it)
            }.launchIn(this)
        }
    }

    fun togglePostStatus(postId: String, isPaused: Boolean) {
        viewModelScope.launch {
            postRepository.togglePostStatus(postId, isPaused).onEach {
                _actionState.emit(it)
            }.launchIn(this)
        }
    }

    fun markAsResolved(postId: String) {
        viewModelScope.launch {
            postRepository.markAsResolved(postId).onEach {
                _actionState.emit(it)
            }.launchIn(this)
        }
    }
}
