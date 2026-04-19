package com.pethelp.app.features.post.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.domain.model.*
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPostUiState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: UiText? = null,
    
    // Editable fields
    val title: String = "",
    val description: String = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val status: PostStatus = PostStatus.ACTIVE,
    val animalType: String = "",
    val breed: String = "",
    val age: AnimalAge = AnimalAge.YOUNG,
    val gender: AnimalGender = AnimalGender.UNKNOWN,
    val size: AnimalSize = AnimalSize.MEDIUM,
    val vaccinated: Boolean = false,
    val dewormed: Boolean = false,
    val sterilized: Boolean = false,
    val specialCares: Boolean = false,
    val behavior: List<PetBehavior> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val locationName: String = "",
    val street: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditPostEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class EditPostEvent {
        data class ShowSnackbar(val message: UiText) : EditPostEvent()
        object PostUpdated : EditPostEvent()
    }

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
                        resource.data?.let { post ->
                            _uiState.update {
                                it.copy(
                                    post = post,
                                    isLoading = false,
                                    title = post.title,
                                    description = post.description,
                                    category = post.category,
                                    status = post.status,
                                    animalType = post.animalType,
                                    breed = post.breed,
                                    age = post.age,
                                    gender = post.gender,
                                    size = post.size,
                                    vaccinated = post.vaccinated,
                                    dewormed = post.dewormed,
                                    sterilized = post.sterilized,
                                    specialCares = post.specialCares,
                                    behavior = post.behavior,
                                    imageUrls = post.imageUrls,
                                    locationName = post.locationName,
                                    street = post.street,
                                    neighborhood = post.neighborhood,
                                    city = post.city,
                                    latitude = post.latitude,
                                    longitude = post.longitude
                                )
                            }
                        } ?: run {
                            _uiState.update { it.copy(isLoading = false, error = UiText.DynamicString("No se encontró la publicación")) }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resource.uiText) }
                    }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= 500) {
            _uiState.update { it.copy(description = newDescription) }
        }
    }

    fun onAnimalTypeChange(type: String) {
        _uiState.update { it.copy(animalType = type) }
    }

    fun onAgeChange(age: AnimalAge) {
        _uiState.update { it.copy(age = age) }
    }

    fun onGenderChange(gender: AnimalGender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onSizeChange(size: AnimalSize) {
        _uiState.update { it.copy(size = size) }
    }

    fun onStatusChange(status: PostStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onVaccinatedChange(value: Boolean) {
        _uiState.update { it.copy(vaccinated = value) }
    }

    fun onDewormedChange(value: Boolean) {
        _uiState.update { it.copy(dewormed = value) }
    }

    fun onSterilizedChange(value: Boolean) {
        _uiState.update { it.copy(sterilized = value) }
    }

    fun onSpecialCaresChange(value: Boolean) {
        _uiState.update { it.copy(specialCares = value) }
    }

    fun improveDescriptionWithAI() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) } // Reuse isSaving or add isProcessingAI
            // Simulate AI call
            kotlinx.coroutines.delay(1500)
            val currentDesc = _uiState.value.description
            if (currentDesc.isNotBlank()) {
                val improvedDesc = "✨ Descripción optimizada por PetHelp IA ✨\n\n$currentDesc\n\nEsta adorable mascota busca un hogar lleno de amor. Es muy sociable y está esperando por ti."
                _uiState.update { it.copy(description = improvedDesc.take(500), isSaving = false) }
                _eventFlow.emit(EditPostEvent.ShowSnackbar(UiText.DynamicString("¡Descripción mejorada con éxito!")))
            } else {
                _uiState.update { it.copy(isSaving = false) }
                _eventFlow.emit(EditPostEvent.ShowSnackbar(UiText.DynamicString("Escribe algo primero para poder mejorarlo.")))
            }
        }
    }

    fun addImage(uri: String) {
        _uiState.update { it.copy(imageUrls = it.imageUrls + uri) }
    }

    fun removeImage(imageUrl: String) {
        _uiState.update { it.copy(imageUrls = it.imageUrls.filter { url -> url != imageUrl }) }
    }

    fun onShowSnackbar(message: String) {
        viewModelScope.launch {
            _eventFlow.emit(EditPostEvent.ShowSnackbar(UiText.DynamicString(message)))
        }
    }

    fun savePost() {
        val currentState = _uiState.value
        val currentPost = currentState.post ?: return

        val updatedPost = currentPost.copy(
            title = currentState.title,
            description = currentState.description,
            status = currentState.status,
            animalType = currentState.animalType,
            age = currentState.age,
            gender = currentState.gender,
            size = currentState.size,
            vaccinated = currentState.vaccinated,
            dewormed = currentState.dewormed,
            sterilized = currentState.sterilized,
            specialCares = currentState.specialCares,
            imageUrls = currentState.imageUrls,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            postRepository.updatePost(updatedPost).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false) }
                        _eventFlow.emit(EditPostEvent.PostUpdated)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false) }
                        _eventFlow.emit(EditPostEvent.ShowSnackbar(resource.uiText ?: UiText.DynamicString("Error al guardar cambios")))
                    }
                }
            }
        }
    }
}
