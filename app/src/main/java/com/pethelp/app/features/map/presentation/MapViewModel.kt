package com.pethelp.app.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
import com.pethelp.app.core.domain.model.PostCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val postsState = _postsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<UiText>(UiText.StringResource(R.string.filter_all))
    val selectedCategory = _selectedCategory.asStateFlow()

    // Publicaciones filtradas por búsqueda y categoría
    val filteredPosts = combine(
        repository.getPosts(), // Aquí idealmente traeríamos todas las verificadas
        _searchQuery,
        _selectedCategory
    ) { resource, query, categoryUiText ->
        when (resource) {
            is Resource.Success -> {
                val filtered = resource.data?.filter { post ->
                    val matchesQuery = post.title.contains(query, ignoreCase = true) || 
                                     post.description.contains(query, ignoreCase = true) ||
                                     post.city.contains(query, ignoreCase = true)
                    
                    // REFACTOR: Usar ID del recurso para comparar si es "Todos"
                    val isAll = categoryUiText is UiText.StringResource && categoryUiText.resId == R.string.filter_all
                    
                    val matchesCategory = isAll || (categoryUiText is UiText.StringResource && 
                        UiText.fromCategory(post.category).let { it is UiText.StringResource && it.resId == categoryUiText.resId })
                    
                    matchesQuery && matchesCategory
                } ?: emptyList()
                Resource.Success(filtered)
            }
            is Resource.Error -> Resource.Error(resource.uiText ?: UiText.StringResource(R.string.error_generic))
            is Resource.Loading -> Resource.Loading()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    // Categorías dinámicas basadas en las publicaciones existentes
    val availableCategories = repository.getPosts().map { resource ->
        if (resource is Resource.Success) {
            val cats = resource.data?.map { UiText.fromCategory(it.category) }?.distinctBy { 
                if (it is UiText.StringResource) it.resId else it.hashCode() 
            } ?: emptyList()
            listOf(UiText.StringResource(R.string.filter_all)) + cats
        } else {
            listOf(UiText.StringResource(R.string.filter_all))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(UiText.StringResource(R.string.filter_all)))

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: UiText) {
        _selectedCategory.value = category
    }
}
