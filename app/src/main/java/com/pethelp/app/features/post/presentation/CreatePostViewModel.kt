package com.pethelp.app.features.post.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.uiText
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.AnimalAge
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.PetBehavior
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.upload.ImageUploader
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CreatePostUiState(
    val title: String = "",
    val description: String = "",
    val category: PostCategory = PostCategory.ADOPTION,
    val animalType: String = "Perro",
    val breed: String = "",
    val age: AnimalAge = AnimalAge.YOUNG,
    val gender: AnimalGender = AnimalGender.UNKNOWN,
    val size: AnimalSize = AnimalSize.MEDIUM,
    val vaccinated: Boolean = false,
    val dewormed: Boolean = false,
    val sterilized: Boolean = false,
    val behavior: List<PetBehavior> = emptyList(),
    val imageUris: List<Uri> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val street: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdPostId: String? = null,
    val error: UiText? = null
)

/**
 * ViewModel para la pantalla de creación de publicaciones.
 *
 * Esta clase se encarga de:
 * 1. Mantener el estado de la UI (`CreatePostUiState`).
 * 2. Validar los datos ingresados por el usuario.
 * 3. Subir las imágenes seleccionadas a Cloudinary (a través de [ImageUploader]).
 * 4. Crear la publicación en Firestore usando [PostRepository].
 * 5. Emitir mensajes de snacks y cambios de estado de carga.
 *
 * El flujo típico es:
 * - El ViewModel recibe actualizaciones de UI (título, descripción, imágenes, etc.).
 * - Al invocar `createPost()`, valida los campos y luego sube las imágenes.
 * - Una vez obtenidas las URLs de Cloudinary, crea el `Post` y lo persiste en Firestore.
 *
 * Esta clase no conoce detalles de la UI (Compose). Solo expone `StateFlow` y
 * `SharedFlow`, que las pantallas observan para renderizar y mostrar mensajes.
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val imageUploader: ImageUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

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

    fun updateDewormed(dewormed: Boolean) {
        _uiState.value = _uiState.value.copy(dewormed = dewormed)
    }

    fun updateSterilized(sterilized: Boolean) {
        _uiState.value = _uiState.value.copy(sterilized = sterilized)
    }

    fun updateAge(age: AnimalAge) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun updateGender(gender: AnimalGender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun toggleBehavior(behavior: PetBehavior) {
        val currentBehaviors = _uiState.value.behavior.toMutableList()
        if (currentBehaviors.contains(behavior)) {
            currentBehaviors.remove(behavior)
        } else {
            currentBehaviors.add(behavior)
        }
        _uiState.value = _uiState.value.copy(behavior = currentBehaviors)
    }

    fun updateAddress(street: String, neighborhood: String, city: String) {
        _uiState.value = _uiState.value.copy(
            street = street,
            neighborhood = neighborhood,
            city = city
        )
    }

    fun addImage(uri: Uri) {
        val current = _uiState.value.imageUris
        if (current.size >= Constants.MAX_IMAGES_PER_POST) {
            viewModelScope.launch {
                _snackbarMessage.emit(UiText.DynamicString("Máximo ${Constants.MAX_IMAGES_PER_POST} fotos permitidas."))
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
            viewModelScope.launch { _snackbarMessage.emit(UiText.DynamicString("Debes iniciar sesión para publicar.")) }
            return
        }

        // Validaciones
        if (state.title.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.DynamicString("Ingresa un título para la publicación.")) }
            return
        }
        if (state.description.isBlank()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.DynamicString("Ingresa una descripción.")) }
            return
        }
        if (state.imageUris.isEmpty()) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.DynamicString("Agrega al menos una foto para continuar.")) }
            return
        }
        if (state.latitude == 0.0 && state.longitude == 0.0) {
            viewModelScope.launch { _snackbarMessage.emit(UiText.DynamicString("Selecciona una ubicación en el mapa.")) }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val uploadedUrls = if (state.imageUris.isNotEmpty()) {
                    val total = state.imageUris.size
                    state.imageUris.mapIndexed { index, uri ->
                        _snackbarMessage.emit(UiText.DynamicString("Subiendo foto ${index + 1} de $total..."))
                        imageUploader.uploadImage(
                            localUri = uri.toString(),
                            folder = Constants.CLOUDINARY_FOLDER_POSTS
                        )
                    }
                } else {
                    emptyList()
                }

                val post = Post(
                    authorId = currentUser.uid,
                    authorName = getCurrentAuthorName(currentUser.uid, currentUser.displayName),
                    authorPhotoUrl = getCurrentAuthorPhotoUrl(currentUser.uid),
                    title = state.title.trim(),
                    description = state.description.trim(),
                    category = state.category,
                    animalType = state.animalType,
                    breed = state.breed.trim(),
                    age = state.age,
                    gender = state.gender,
                    size = state.size,
                    vaccinated = state.vaccinated,
                    dewormed = state.dewormed,
                    sterilized = state.sterilized,
                    behavior = state.behavior,
                    imageUrls = uploadedUrls,
                    street = state.street,
                    neighborhood = state.neighborhood,
                    city = state.city,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationName = state.locationName
                )

                postRepository.createPost(post).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false, 
                                isSuccess = true,
                                createdPostId = resource.data?.id
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.uiText
                            )
                            _snackbarMessage.emit(resource.uiText ?: UiText.DynamicString("Error al crear la publicación."))
                        }
                    }
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "No se pudieron subir las imágenes."
                _uiState.value = _uiState.value.copy(isLoading = false, error = UiText.DynamicString(message))
                _snackbarMessage.emit(UiText.DynamicString(message))
            }
        }
    }

    private suspend fun getCurrentAuthorName(userId: String, fallback: String?): String {
        val fromFirestore = firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .await()
            .getString("name")
            ?.trim()
            .orEmpty()

        return when {
            fromFirestore.isNotBlank() -> fromFirestore
            !fallback.isNullOrBlank() -> fallback
            else -> "Usuario"
        }
    }

    private suspend fun getCurrentAuthorPhotoUrl(userId: String): String {
        return firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .await()
            .getString("photoUrl")
            ?.trim()
            .orEmpty()
    }
}
