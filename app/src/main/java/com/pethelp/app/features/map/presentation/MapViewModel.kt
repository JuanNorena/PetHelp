package com.pethelp.app.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val postsState = _postsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Publicaciones filtradas por búsqueda y categoría
    val filteredPosts = combine(
        repository.getPosts(), // Aquí idealmente traeríamos todas las verificadas
        _searchQuery,
        _selectedCategory
    ) { resource, query, category ->
        when (resource) {
            is Resource.Success -> {
                val filtered = resource.data?.filter { post ->
                    val matchesQuery = post.title.contains(query, ignoreCase = true) || 
                                     post.description.contains(query, ignoreCase = true) ||
                                     post.city.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category == "Todos" || post.category.displayName == category
                    
                    matchesQuery && matchesCategory
                } ?: emptyList()
                Resource.Success(filtered)
            }
            is Resource.Error -> Resource.Error(resource.message ?: "Error desconocido")
            is Resource.Loading -> Resource.Loading()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    // Categorías dinámicas basadas en las publicaciones existentes
    val availableCategories = repository.getPosts().map { resource ->
        if (resource is Resource.Success) {
            val cats = resource.data?.map { it.category.displayName }?.distinct()?.sorted() ?: emptyList()
            listOf("Todos") + cats
        } else {
            listOf("Todos")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Todos"))

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }
}
