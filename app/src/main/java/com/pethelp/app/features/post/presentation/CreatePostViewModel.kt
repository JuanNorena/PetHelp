package com.pethelp.app.features.post.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePostUiState(
    val title: String = "",
    val description: String = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val animalType: String = "Perro",
    val breed: String = "",
    val size: AnimalSize = AnimalSize.MEDIUM,
    val vaccinated: Boolean = false,
    val imageUris: List<Uri> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateCategory(category: PostCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateAnimalType(animalType: String) {
        _uiState.value = _uiState.value.copy(animalType = animalType)
    }

    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(breed = breed)
    }

    fun updateSize(size: AnimalSize) {
        _uiState.value = _uiState.value.copy(size = size)
    }

    fun updateVaccinated(vaccinated: Boolean) {
        _uiState.value = _uiState.value.copy(vaccinated = vaccinated)
    }

    fun addImage(uri: Uri) {
        val current = _uiState.value.imageUris
        if (current.size >= Constants.MAX_IMAGES_PER_POST) {
            viewModelScope.launch {
                _snackbarMessage.emit("Máximo ${Constants.MAX_IMAGES_PER_POST} fotos permitidas.")
            }
            return
        }
        _uiState.value = _uiState.value.copy(imageUris = current + uri)
    }

    fun removeImage(index: Int) {
        val current = _uiState.value.imageUris.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(imageUris = current)
        }
    }

    fun updateLocation(lat: Double, lng: Double, name: String) {
        _uiState.value = _uiState.value.copy(
            latitude = lat,
            longitude = lng,
            locationName = name
        )
    }

    fun createPost() {
        val state = _uiState.value
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            viewModelScope.launch { _snackbarMessage.emit("Debes iniciar sesión para publicar.") }
            return
        }

        // Validaciones
        if (state.title.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit("Ingresa un título para la publicación.") }
            return
        }
        if (state.description.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit("Ingresa una descripción.") }
            return
        }

        val post = Post(
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Usuario",
            title = state.title.trim(),
            description = state.description.trim(),
            category = state.category,
            animalType = state.animalType,
            breed = state.breed.trim(),
            size = state.size,
            vaccinated = state.vaccinated,
            imageUrls = state.imageUrls,
            latitude = state.latitude,
            longitude = state.longitude,
            locationName = state.locationName
        )

        viewModelScope.launch {
            postRepository.createPost(post).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                        _snackbarMessage.emit("¡Publicación creada exitosamente!")
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                        _snackbarMessage.emit(resource.message ?: "Error al crear la publicación.")
                    }
                }
            }
        }
    }
}
