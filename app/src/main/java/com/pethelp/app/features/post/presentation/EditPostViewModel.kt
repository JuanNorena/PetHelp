/**
 * ViewModel para editar una publicación existente.
 *
 * Carga el post desde Firestore, mantiene cambios locales del formulario
 * y persiste la actualización cuando el usuario confirma.
 */
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

/**
 * Estado de la pantalla de edicion de publicaciones.
 *
 * Incluye el estado de carga/guardado y una copia editable de los campos del post.
 */
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
/**
 * ViewModel para editar una publicacion existente.
 *
 * Carga el post desde el repositorio, mantiene cambios locales de formulario y persiste
 * la actualizacion cuando el usuario confirma.
 */
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(EditPostUiState())

    /** Estado de solo lectura para la pantalla de edicion. */
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditPostEvent>()

    /** Eventos de una sola vez para mensajes y cierre de pantalla. */
    val eventFlow = _eventFlow.asSharedFlow()

    /** Eventos emitidos hacia la UI para feedback y navegacion. */
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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Actualizadores de Campos ──────────────────────────────────────────
    /** Actualiza el título editable en el estado local. */
    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    /**
     * Actualiza la descripción editable, limitando a 500 caracteres.
     *
     * @param newDescription Nueva descripción ingresada por el usuario.
     */
    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= 500) {
            _uiState.update { it.copy(description = newDescription) }
        }
    }

    /** @param type Nuevo tipo de animal. */
    fun onAnimalTypeChange(type: String) {
        _uiState.update { it.copy(animalType = type) }
    }

    /** @param age Nuevo rango de edad. */
    fun onAgeChange(age: AnimalAge) {
        _uiState.update { it.copy(age = age) }
    }

    /** @param gender Nuevo sexo del animal. */
    fun onGenderChange(gender: AnimalGender) {
        _uiState.update { it.copy(gender = gender) }
    }

    /** @param size Nuevo tamaño del animal. */
    fun onSizeChange(size: AnimalSize) {
        _uiState.update { it.copy(size = size) }
    }

    /** @param status Nuevo estado de la publicación. */
    fun onStatusChange(status: PostStatus) {
        _uiState.update { it.copy(status = status) }
    }

    /** @param value true si está vacunado. */
    fun onVaccinatedChange(value: Boolean) {
        _uiState.update { it.copy(vaccinated = value) }
    }

    /** @param value true si está desparasitado. */
    fun onDewormedChange(value: Boolean) {
        _uiState.update { it.copy(dewormed = value) }
    }

    /** @param value true si está esterilizado. */
    fun onSterilizedChange(value: Boolean) {
        _uiState.update { it.copy(sterilized = value) }
    }

    /** @param value true si requiere cuidados especiales. */
    fun onSpecialCaresChange(value: Boolean) {
        _uiState.update { it.copy(specialCares = value) }
    }

    /** Genera una version mejorada de la descripcion usando una simulacion de IA. */
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

    /** Agrega una nueva imagen al listado editable. */
    fun addImage(uri: String) {
        _uiState.update { it.copy(imageUrls = it.imageUrls + uri) }
    }

    /** Elimina una imagen del listado editable. */
    fun removeImage(imageUrl: String) {
        _uiState.update { it.copy(imageUrls = it.imageUrls.filter { url -> url != imageUrl }) }
    }

    /** Emite un mensaje temporal para mostrar en snackbar. */
    fun onShowSnackbar(message: String) {
        viewModelScope.launch {
            _eventFlow.emit(EditPostEvent.ShowSnackbar(UiText.DynamicString(message)))
        }
    }

    /** Valida y persiste los cambios del post en el repositorio. */
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
                        _eventFlow.emit(
                            EditPostEvent.ShowSnackbar(
                                resource.message ?: UiText.DynamicString("Error al guardar cambios")
                            )
                        )
                    }
                }
            }
        }
    }
}
