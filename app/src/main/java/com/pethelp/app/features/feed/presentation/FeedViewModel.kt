/**
 * ViewModel de la pantalla principal de Feed (muro de publicaciones).
 *
 * Carga publicaciones verificadas desde [PostRepository], filtra por categoría,
 * busca por texto y gestiona acciones de favorito y voto.
 */
package com.pethelp.app.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.features.auth.domain.repository.AuthRepository
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Modelo de estado para la pantalla de Feed.
 *
 * Representa de forma inmutable todo lo que el usuario ve en la pantalla principal de noticias.
 *
 * **Arquitectura:**
 * Al ser una `data class`, facilita el uso de la función `.copy()` para realizar transiciones de estado
 * atómicas y seguras entre hilos.
 *
 * @property allPublicPosts Lista completa de todas las publicaciones verificadas recuperadas del servidor.
 * @property selectedCategory Categoría opcional seleccionada por el usuario para filtrar la lista.
 * @property isLoading Indica si hay una petición de red en curso para mostrar un progreso en la UI.
 * @property error Contiene el mensaje de error localizado si algo falla durante la carga.
 */
data class FeedUiState(
    val allPublicPosts: List<Post> = emptyList(),
    val selectedCategory: PostCategory? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val favoritesSet: Set<String> = emptySet()
) {
    /**
     * Propiedad derivada que calcula la lista de publicaciones a mostrar.
     *
     * **Lógica Paso a Paso:**
     * 1. Verifica si hay un [selectedCategory] activo.
     * 2. Si es nulo (Filtro "Todos"), retorna la lista completa.
     * 3. Si tiene valor, filtra [allPublicPosts] comparando la categoría de cada post.
     *
     * @return Una lista filtrada de objetos [Post].
     */
    val filteredPosts: List<Post>
        get() = if (selectedCategory == null) {
            allPublicPosts
        } else {
            allPublicPosts.filter { it.category == selectedCategory }
        }
}

/**
 * ViewModel encargado de la lógica de negocio del Feed de publicaciones.
 *
 * **Responsabilidad Principal:**
 * Orquestar la recuperación de datos desde el [PostRepository], aplicar reglas de negocio
 * (como mostrar solo posts verificados y ordenados) y exponer un estado reactivo a la [FeedScreen].
 *
 * **Patrones de Diseño:**
 * - **Unidirectional Data Flow (UDF):** El ViewModel emite estados y la UI dispara eventos.
 * - **Encapsulamiento:** Protege el estado mutable para que solo el ViewModel pueda modificarlo.
 *
 * **Notas para Junior Developers:**
 * - Se utiliza [viewModelScope] para que las corrutinas de red se cancelen automáticamente si el usuario
 *   sale de la aplicación o cambia de pantalla.
 * - El método [.update] de `MutableStateFlow` es la forma recomendada y segura de cambiar estados
 *   concurrentes.
 *
 * @property postRepository Repositorio inyectado para acceder a la persistencia de publicaciones.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see FeedUiState Para conocer la estructura del estado de la interfaz.
 * @see PostRepository Para ver la implementación de los servicios de datos.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Estado Interno y Público ─────────────────────────────────────────────
    /** Fuente de verdad mutable. Se inicializa en estado de carga. */
    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))

    /** Flujo de solo lectura que la UI observa mediante `collectAsStateWithLifecycle`. */
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    /**
     * Bloque de inicialización.
     * Se ejecuta inmediatamente al crear el ViewModel para asegurar que el usuario vea datos
     * apenas entra a la pantalla.
     */
    init {
        loadPublicPosts()
        observeUserFavorites()
    }

    /**
     * Observa los favoritos del usuario autenticado y sincroniza [FeedUiState.favoritesSet].
     *
     * Se suscribe al perfil del usuario vía [AuthRepository.getCurrentUser] y,
     * una vez obtenido el UID, escucha [PostRepository.getFavoritePosts] para
     * mantener actualizado el set de IDs favoritos sin necesidad de recargar
     * el feed completo.
     */
    private fun observeUserFavorites() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { resource ->
                if (resource is Resource.Success) {
                    val userId = resource.data?.id ?: return@collect
                    postRepository.getFavoritePosts(userId).collect { favRes ->
                        if (favRes is Resource.Success) {
                            val ids = (favRes.data ?: emptyList()).map { it.id }.toSet()
                            _uiState.update { it.copy(favoritesSet = ids) }
                        }
                    }
                }
            }
        }
    }

    /** Alterna favorito para el usuario actual. */
    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser().first().data?.id ?: return@launch
            val isCurrentlyFavorite = _uiState.value.favoritesSet.contains(postId)
            postRepository.toggleFavorite(postId, userId, isCurrentlyFavorite).collect { resource ->
                if (resource is Resource.Success) {
                    val newSet = _uiState.value.favoritesSet.toMutableSet()
                    if (isCurrentlyFavorite) newSet.remove(postId) else newSet.add(postId)
                    _uiState.update { it.copy(favoritesSet = newSet) }
                }
            }
        }
    }

    // ── Gestión de Datos ─────────────────────────────────────────────────────
    /**
     * Carga de forma asíncrona las publicaciones desde el repositorio.
     *
     * **Procesamiento de Datos (Reglas de Negocio):**
     * 1. **Carga:** Marca el estado como `isLoading = true`.
     * 2. **Filtro de Seguridad:** Solo se aceptan publicaciones con estado [PostStatus.VERIFIED].
     * 3. **Ordenamiento:** Las publicaciones más recientes (mayor [Post.createdAt]) aparecen primero.
     * 4. **Manejo de Errores:** Captura fallos de red y emite un [UiText] amigable.
     *
     * @since 1.0.0
     * @author Equipo de Desarrollo PetHelp
     */
    fun loadPublicPosts() {
        viewModelScope.launch {
            // PASO 1: Iniciar recolección del flujo de datos del repositorio.
            postRepository.getPosts(category = null).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // PASO 2: Actualizar UI para mostrar el spinner de carga.
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        // PASO 3: Transformar los datos crudos según reglas de negocio.
                        val publicPosts = (resource.data ?: emptyList())
                            .filter { it.status == PostStatus.VERIFIED } // Seguridad: solo verificados.
                            .sortedByDescending { it.createdAt } // Orden cronológico inverso.

                        // PASO 4: Publicar el nuevo estado de éxito.
                        _uiState.update {
                            it.copy(
                                allPublicPosts = publicPosts,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        // PASO 5: Gestionar fallos (Sin conexión, error de servidor, etc).
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.uiText ?: UiText.StringResource(R.string.error_generic)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Lógica de Filtrado ───────────────────────────────────────────────────
    /**
     * Actualiza la categoría seleccionada para filtrar el feed.
     *
     * Al actualizar [FeedUiState.selectedCategory], la propiedad [FeedUiState.filteredPosts]
     * se recalcula automáticamente en la UI debido a la reactividad de Compose.
     *
     * @param category La categoría deseada o `null` para mostrar todas.
     */
    fun selectCategory(category: PostCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
