package com.pethelp.app.features.post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de publicaciones favoritas.
 */
data class FavoritesUiState(
    val posts: List<Post> = emptyList(),
    val filteredPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isGridView: Boolean = true,
    val selectedCategory: PostCategory? = null
)

@HiltViewModel
/**
 * ViewModel que administra el listado de favoritos del usuario autenticado.
 *
 * Se encarga de cargar favoritos, aplicar filtros por categoria y alternar el modo
 * de visualizacion entre grid y lista.
 */
class FavoritesViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())

    /** Estado de solo lectura consumido por la UI. */
    val uiState = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()

    /** Flujo de mensajes temporales para feedback al usuario. */
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        loadFavorites()
    }

    /** Carga las publicaciones marcadas como favoritas por el usuario actual. */
    fun loadFavorites() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { resource ->
                if (resource is Resource.Success) {
                    val userId = resource.data?.id ?: return@collect
                    postRepository.getFavoritePosts(userId).collect { postResource ->
                        when (postResource) {
                            is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Resource.Success -> _uiState.update { 
                                val posts = postResource.data ?: emptyList()
                                it.copy(
                                    posts = posts,
                                    filteredPosts = filterPosts(posts, it.selectedCategory),
                                    isLoading = false,
                                    error = null
                                ) 
                            }
                            is Resource.Error -> _uiState.update { 
                                it.copy(isLoading = false, error = postResource.uiText)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Alterna entre vista en grilla y vista en lista. */
    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    /** Selecciona categoria y recalcula el subconjunto filtrado. */
    fun selectCategory(category: PostCategory?) {
        _uiState.update { 
            it.copy(
                selectedCategory = category,
                filteredPosts = filterPosts(it.posts, category)
            ) 
        }
    }

    private fun filterPosts(posts: List<Post>, category: PostCategory?): List<Post> {
        return if (category == null) {
            posts
        } else {
            posts.filter { it.category == category }
        }
    }

    /** Elimina un post de favoritos y refresca la lista actual. */
    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser().first().data?.id ?: return@launch
            postRepository.toggleFavorite(postId, userId, false).collect { resource ->
                if (resource is Resource.Success) {
                    // Refresh list after removing favorite
                    loadFavorites()
                }
            }
        }
    }
}
