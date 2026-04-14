package com.pethelp.app.features.post.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.common.Resource
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPostUiState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    // Editable fields
    val title: String = "",
    val description: String = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val animalType: String = "",
    val breed: String = "",
    val age: AnimalAge = AnimalAge.YOUNG,
    val gender: AnimalGender = AnimalGender.UNKNOWN,
    val size: AnimalSize = AnimalSize.MEDIUM,
    val vaccinated: Boolean = false,
    val dewormed: Boolean = false,
    val sterilized: Boolean = false,
    val behavior: List<PetBehavior> = emptyList(),
    val street: String = "",
    val neighborhood: String = "",
    val city: String = ""
)

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPostById(postId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val post = resource.data
                        if (post != null) {
                            _uiState.update {
                                it.copy(
                                    post = post,
                                    isLoading = false,
                                    title = post.title,
                                    description = post.description,
                                    category = post.category,
                                    animalType = post.animalType,
                                    breed = post.breed,
                                    age = post.age,
                                    gender = post.gender,
                                    size = post.size,
                                    vaccinated = post.vaccinated,
                                    dewormed = post.dewormed,
                                    sterilized = post.sterilized,
                                    behavior = post.behavior,
                                    street = post.street,
                                    neighborhood = post.neighborhood,
                                    city = post.city
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "Post not found") }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.message) }
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCategory(category: PostCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateAnimalType(type: String) {
        _uiState.update { it.copy(animalType = type) }
    }

    fun updateBreed(breed: String) {
        _uiState.update { it.copy(breed = breed) }
    }

    fun updateAge(age: AnimalAge) {
        _uiState.update { it.copy(age = age) }
    }

    fun updateGender(gender: AnimalGender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateSize(size: AnimalSize) {
        _uiState.update { it.copy(size = size) }
    }

    fun updateVaccinated(vaccinated: Boolean) {
        _uiState.update { it.copy(vaccinated = vaccinated) }
    }

    fun updateDewormed(dewormed: Boolean) {
        _uiState.update { it.copy(dewormed = dewormed) }
    }

    fun updateSterilized(sterilized: Boolean) {
        _uiState.update { it.copy(sterilized = sterilized) }
    }

    fun toggleBehavior(behavior: PetBehavior) {
        _uiState.update { currentState ->
            val currentBehaviors = currentState.behavior.toMutableList()
            if (currentBehaviors.contains(behavior)) {
                currentBehaviors.remove(behavior)
            } else {
                currentBehaviors.add(behavior)
            }
            currentState.copy(behavior = currentBehaviors)
        }
    }

    fun updateLocation(street: String, neighborhood: String, city: String) {
        _uiState.update { it.copy(street = street, neighborhood = neighborhood, city = city) }
    }

    fun saveChanges() {
        val currentPost = _uiState.value.post ?: return
        val updatedPost = currentPost.copy(
            title = _uiState.value.title,
            description = _uiState.value.description,
            category = _uiState.value.category,
            animalType = _uiState.value.animalType,
            breed = _uiState.value.breed,
            age = _uiState.value.age,
            gender = _uiState.value.gender,
            size = _uiState.value.size,
            vaccinated = _uiState.value.vaccinated,
            dewormed = _uiState.value.dewormed,
            sterilized = _uiState.value.sterilized,
            behavior = _uiState.value.behavior,
            street = _uiState.value.street,
            neighborhood = _uiState.value.neighborhood,
            city = _uiState.value.city,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            postRepository.updatePost(updatedPost).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        _snackbarMessage.emit("Cambios guardados exitosamente")
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.message) }
                        _snackbarMessage.emit(resource.message ?: "Error al guardar cambios")
                    }
                }
            }
        }
    }
}
