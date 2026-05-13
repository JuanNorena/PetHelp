/**
 * ViewModel para la creación de publicaciones de mascotas.
 *
 * Mantiene el estado del formulario multi-paso, sube imágenes a Cloudinary,
 * sugiere categoría con IA y persiste la publicación en Firestore.
 */
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
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estado inmutable del formulario de creación de publicaciones.
 *
 * Agrupa todos los campos editables por el usuario en el flujo multi-paso
 * de creación de un post, junto con el estado de carga y sugerencias de IA.
 *
 * @param title Título de la publicación.
 * @param description Descripción detallada de la mascota.
 * @param category Categoría de la publicación (adopción, perdido, etc.).
 * @param animalType Tipo de animal (Perro, Gato, etc.).
 * @param breed Raza del animal.
 * @param age Rango de edad del animal.
 * @param gender Sexo del animal.
 * @param size Tamaño del animal.
 * @param vaccinated Indica si está vacunado.
 * @param dewormed Indica si está desparasitado.
 * @param sterilized Indica si está esterilizado.
 * @param behavior Lista de comportamientos seleccionados.
 * @param imageUris URIs locales de imágenes seleccionadas.
 * @param imageUrls URLs de imágenes ya subidas a Cloudinary.
 * @param isLoading Indica si hay una operación en curso.
 * @param error Mensaje de error si ocurre un fallo.
 * @param aiSuggestedCategory Categoría sugerida por IA.
 */
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
    val isSuggestingCategory: Boolean = false,
    val aiCategoryReason: String = "",
    val aiCategoryConfidence: Int? = null,
    val aiCategoryError: String? = null,
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
    private val imageUploader: ImageUploader,
    private val aiChatRepository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    /** Job activo para debounce de sugerencia de categoría por IA. */
    private var categorySuggestionJob: Job? = null

    // ── Actualizadores de Campos del Formulario ─────────────────────────────
    /**
     * Actualiza el título de la publicación y agenda sugerencia de categoría.
     *
     * @param title Nuevo título ingresado por el usuario.
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        scheduleCategorySuggestion()
    }

    /**
     * Actualiza la descripción de la publicación y agenda sugerencia de categoría.
     *
     * @param description Nueva descripción ingresada por el usuario.
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
        scheduleCategorySuggestion()
    }

    /**
     * Actualiza la categoría seleccionada manualmente.
     *
     * @param category Categoría elegida por el usuario.
     */
    fun updateCategory(category: PostCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    /**
     * Actualiza el tipo de animal y agenda sugerencia de categoría.
     *
     * @param animalType Tipo de mascota (ej. "Perro", "Gato").
     */
    fun updateAnimalType(animalType: String) {
        _uiState.value = _uiState.value.copy(animalType = animalType)
        scheduleCategorySuggestion()
    }

    /**
     * Actualiza la raza del animal.
     *
     * @param breed Raza de la mascota.
     */
    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(breed = breed)
    }

    /**
     * Actualiza el tamaño del animal.
     *
     * @param size Tamaño de la mascota.
     */
    fun updateSize(size: AnimalSize) {
        _uiState.value = _uiState.value.copy(size = size)
    }

    /** @param vaccinated true si la mascota está vacunada. */
    fun updateVaccinated(vaccinated: Boolean) {
        _uiState.value = _uiState.value.copy(vaccinated = vaccinated)
    }

    /** @param dewormed true si la mascota está desparasitada. */
    fun updateDewormed(dewormed: Boolean) {
        _uiState.value = _uiState.value.copy(dewormed = dewormed)
    }

    /** @param sterilized true si la mascota está esterilizada. */
    fun updateSterilized(sterilized: Boolean) {
        _uiState.value = _uiState.value.copy(sterilized = sterilized)
    }

    /**
     * Actualiza el rango de edad del animal.
     *
     * @param age Edad seleccionada.
     */
    fun updateAge(age: AnimalAge) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    /**
     * Actualiza el sexo del animal.
     *
     * @param gender Sexo seleccionado.
     */
    fun updateGender(gender: AnimalGender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    /**
     * Alterna un comportamiento en la lista de comportamientos del animal.
     *
     * Si el comportamiento ya existe, lo elimina; si no, lo agrega.
     *
     * @param behavior Comportamiento a alternar.
     */
    fun toggleBehavior(behavior: PetBehavior) {
        val currentBehaviors = _uiState.value.behavior.toMutableList()
        if (currentBehaviors.contains(behavior)) {
            currentBehaviors.remove(behavior)
        } else {
            currentBehaviors.add(behavior)
        }
        _uiState.value = _uiState.value.copy(behavior = currentBehaviors)
    }

    /**
     * Actualiza los datos de dirección textual de la publicación.
     *
     * @param street Calle de la ubicación.
     * @param neighborhood Barrio o sector.
     * @param city Ciudad.
     */
    fun updateAddress(street: String, neighborhood: String, city: String) {
        _uiState.value = _uiState.value.copy(
            street = street,
            neighborhood = neighborhood,
            city = city
        )
    }

    /**
     * Agrega una imagen al carrusel de fotos de la publicación.
     *
     * Si se alcanza [Constants.MAX_IMAGES_PER_POST], emite un snackbar
     * de advertencia y no agrega la imagen.
     *
     * @param uri URI local de la imagen seleccionada.
     */
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

    /**
     * Elimina una imagen del carrusel por su índice.
     *
     * @param index Posición de la imagen a eliminar en la lista.
     */
    fun removeImage(index: Int) {
        val current = _uiState.value.imageUris.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(imageUris = current)
        }
    }

    /**
     * Actualiza las coordenadas geográficas y nombre del lugar seleccionado.
     *
     * @param lat Latitud.
     * @param lng Longitud.
     * @param name Nombre descriptivo del lugar.
     */
    fun updateLocation(lat: Double, lng: Double, name: String) {
        _uiState.value = _uiState.value.copy(
            latitude = lat,
            longitude = lng,
            locationName = name
        )
    }

    // ── Creación de Publicación ─────────────────────────────────────────────
    /**
     * Valida el formulario, sube las imágenes a Cloudinary y persiste la publicación en Firestore.
     *
     * **Validaciones:**
     * - Título y descripción no vacíos.
     * - Al menos una imagen seleccionada.
     * - Ubicación seleccionada (latitud/longitud distinta de 0).
     *
     * **Flujo:**
     * 1. Sube cada imagen a Cloudinary reportando progreso por snackbar.
     * 2. Construye el objeto [Post] con las URLs obtenidas.
     * 3. Persiste en Firestore vía [PostRepository.createPost].
     * 4. Emite [CreatePostUiState.isSuccess] para que la UI navegue al resultado.
     */
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

    /**
     * Solicita inmediatamente una sugerencia de categoría al servicio de IA.
     *
     * Cancela cualquier job de debounce pendiente y ejecuta la sugerencia.
     *
     * @param showErrors Si es true, muestra snackbar cuando no hay suficiente texto.
     */
    fun suggestCategoryWithAi(showErrors: Boolean = true) {
        categorySuggestionJob?.cancel()
        requestCategorySuggestion(showErrors = showErrors)
    }

    /**
     * Envía título y descripción al modelo de IA para sugerir la categoría óptima.
     *
     * Requiere al menos 4 caracteres de título y 12 de descripción para evitar
     * llamadas innecesarias con texto incompleto.
     *
     * @param showErrors Si es true, notifica al usuario cuando falta texto.
     */
    private fun requestCategorySuggestion(showErrors: Boolean) {
        val state = _uiState.value
        if (state.title.trim().length < 4 || state.description.trim().length < 12) {
            if (showErrors) {
                viewModelScope.launch {
                    _snackbarMessage.emit(UiText.DynamicString("Agrega titulo y descripcion para sugerir categoria con IA."))
                }
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSuggestingCategory = true,
                aiCategoryError = null
            )

            val result = aiChatRepository.suggestPostCategory(
                title = state.title,
                description = state.description,
                animalType = state.animalType
            )

            result.onSuccess { suggestion ->
                _uiState.value = _uiState.value.copy(
                    category = suggestion.category,
                    isSuggestingCategory = false,
                    aiCategoryReason = suggestion.reason,
                    aiCategoryConfidence = suggestion.confidence,
                    aiCategoryError = null
                )
            }

            result.onFailure { error ->
                val message = error.localizedMessage ?: "No fue posible sugerir categoria con IA."
                _uiState.value = _uiState.value.copy(
                    isSuggestingCategory = false,
                    aiCategoryError = message
                )
                if (showErrors) {
                    _snackbarMessage.emit(UiText.DynamicString(message))
                }
            }
        }
    }

    /**
     * Agenda una sugerencia de categoría con debounce de 900 ms.
     *
     * Solo agenda si hay suficiente texto (título >= 4 chars, descripción >= 24 chars).
     * Cancela el job anterior para evitar múltiples llamadas simultáneas.
     */
    private fun scheduleCategorySuggestion() {
        val state = _uiState.value
        if (state.title.trim().length < 4 || state.description.trim().length < 24) return

        categorySuggestionJob?.cancel()
        categorySuggestionJob = viewModelScope.launch {
            delay(900)
            requestCategorySuggestion(showErrors = false)
        }
    }

    /**
     * Recupera el nombre visible del autor desde Firestore.
     *
     * **Orden de preferencia:**
     * 1. Campo "name" del documento en Firestore.
     * 2. displayName de Firebase Auth.
     * 3. "Usuario" como fallback genérico.
     *
     * @param userId UID del usuario autenticado.
     * @param fallback displayName del usuario desde Firebase Auth.
     * @return Nombre a mostrar como autor de la publicación.
     */
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

    /**
     * Recupera la URL de foto de perfil del autor desde Firestore.
     *
     * @param userId UID del usuario autenticado.
     * @return URL de la foto de perfil o cadena vacía si no existe.
     */
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
