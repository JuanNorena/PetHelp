/**
 * ViewModel del mapa interactivo de publicaciones.
 *
 * Gestiona filtros por categoría y búsqueda por texto, exponiendo
 * un estado reactivo de publicaciones para mostrar como marcadores
 * en el mapa de Google Maps.
 */
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

/**
 * ViewModel encargado de la lógica de negocio para la visualización de mascotas en el mapa.
 *
 * **Responsabilidad Principal:**
 * Gestionar y filtrar dinámicamente las publicaciones de mascotas basándose en la ubicación,
 * términos de búsqueda y categorías seleccionadas por el usuario. Actúa como la fuente de
 * verdad reactiva para la pantalla [MapScreen].
 *
 * **Patrones y Tecnologías:**
 * - **MVVM (Model-View-ViewModel):** Separa la lógica de filtrado de la UI.
 * - **Kotlin Flow (Combine):** Orquesta múltiples fuentes de datos de forma reactiva.
 * - **StateIn:** Convierte flujos fríos en estados calientes persistentes para la UI.
 * - **Clean Architecture:** Se comunica con la capa de dominio a través de [PostRepository].
 *
 * **Lógica de Filtrado Reactivo:**
 * El ViewModel utiliza el operador `combine` para observar simultáneamente:
 * 1. La lista completa de publicaciones desde el repositorio.
 * 2. La cadena de texto de búsqueda ingresada por el usuario.
 * 3. La categoría de filtro seleccionada en los chips de la UI.
 *
 * **Notas para Junior Developers:**
 * - El uso de `MutableStateFlow` privado y `StateFlow` público es fundamental para el encapsulamiento.
 * - `WhileSubscribed(5000)` en `stateIn` es una optimización que mantiene el flujo vivo por 5 segundos
 *   después de que la UI deja de observar, evitando reinicios costosos en rotaciones rápidas de pantalla.
 *
 * @property repository Repositorio inyectado por Hilt para acceder a los datos de las mascotas.
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see PostRepository Implementación de acceso a datos de publicaciones.
 * @see MapScreen Interfaz de usuario que consume este ViewModel.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    // ── Estados Mutables Privados ───────────────────────────────────────────
    /** Estado interno para la carga inicial de todas las publicaciones. */
    private val _postsState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    /** Flujo público para observar el estado base de las publicaciones. */
    val postsState = _postsState.asStateFlow()

    /** Almacena el texto de búsqueda actual ingresado en la barra superior. */
    private val _searchQuery = MutableStateFlow("")
    /** Flujo público de la consulta de búsqueda. */
    val searchQuery = _searchQuery.asStateFlow()

    /** Almacena el filtro de categoría seleccionado (por defecto "Todos"). */
    private val _selectedCategory = MutableStateFlow<UiText>(UiText.StringResource(R.string.filter_all))
    /** Flujo público de la categoría seleccionada. */
    val selectedCategory = _selectedCategory.asStateFlow()

    // ── Lógica de Procesamiento Reactivo ────────────────────────────────────
    /**
     * Flujo de publicaciones filtradas en tiempo real.
     *
     * **PASO 1:** Combina tres fuentes de datos reactivas: Repositorio, Búsqueda y Categoría.
     * **PASO 2:** Valida si el recurso del repositorio es exitoso.
     * **PASO 3:** Aplica filtros combinados (Query + Categoría) sobre cada post.
     * **PASO 4:** Emite un nuevo [Resource.Success] con la lista resultante.
     */
    val filteredPosts = combine(
        repository.getPosts(), // Obtenemos el flujo de datos desde Firebase.
        _searchQuery,
        _selectedCategory
    ) { resource, query, categoryUiText ->
        when (resource) {
            is Resource.Success -> {
                // PASO 3.1: Filtrar por texto (Título, Descripción o Ciudad).
                val filtered = resource.data?.filter { post ->
                    val matchesQuery = post.title.contains(query, ignoreCase = true) || 
                                     post.description.contains(query, ignoreCase = true) ||
                                     post.city.contains(query, ignoreCase = true)
                    
                    // PASO 3.2: Filtrar por Categoría.
                    // Identificamos si el filtro actual es "Todos" comparando el ID del recurso de string.
                    val isAll = categoryUiText is UiText.StringResource && categoryUiText.resId == R.string.filter_all
                    
                    // Comparamos la categoría del post con la seleccionada en la UI.
                    val matchesCategory = isAll || (categoryUiText is UiText.StringResource && 
                        UiText.fromCategory(post.category).let { it is UiText.StringResource && it.resId == categoryUiText.resId })
                    
                    // Solo incluimos el post si cumple AMBOS criterios.
                    matchesQuery && matchesCategory
                } ?: emptyList()

                // PASO 4: Emitir la lista filtrada.
                Resource.Success(filtered)
            }
            // Propagar estados de error o carga si el repositorio no ha terminado.
            is Resource.Error -> Resource.Error(resource.uiText ?: UiText.StringResource(R.string.error_generic))
            is Resource.Loading -> Resource.Loading()
        }
    }.stateIn(
        scope = viewModelScope, 
        started = SharingStarted.WhileSubscribed(5000), 
        initialValue = Resource.Loading()
    )

    /**
     * Genera dinámicamente la lista de categorías disponibles basadas en los posts existentes.
     *
     * **Lógica Paso a Paso:**
     * 1. Observa los posts que llegan del repositorio.
     * 2. Extrae las categorías únicas presentes en las publicaciones.
     * 3. Añade la opción "Todos" al inicio de la lista.
     * 4. Evita duplicados comparando los IDs de recursos de [UiText].
     */
    val availableCategories = repository.getPosts().map { resource ->
        if (resource is Resource.Success) {
            // Extraer categorías únicas de los posts actuales.
            val cats = resource.data?.map { UiText.fromCategory(it.category) }?.distinctBy { 
                if (it is UiText.StringResource) it.resId else it.hashCode() 
            } ?: emptyList()
            
            // Retornar lista combinada con la opción por defecto al inicio.
            listOf(UiText.StringResource(R.string.filter_all)) + cats
        } else {
            // Si hay error o carga, solo mostramos la opción "Todos".
            listOf(UiText.StringResource(R.string.filter_all))
        }
    }.stateIn(
        scope = viewModelScope, 
        started = SharingStarted.WhileSubscribed(5000), 
        initialValue = listOf(UiText.StringResource(R.string.filter_all))
    )

    // ── Eventos de la Interfaz de Usuario ───────────────────────────────────
    /**
     * Actualiza el término de búsqueda global.
     * Dispara automáticamente el recálculo de [filteredPosts].
     *
     * @param query El nuevo texto de búsqueda.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Actualiza la categoría de filtro seleccionada.
     * Dispara automáticamente el recálculo de [filteredPosts].
     *
     * @param category El objeto [UiText] que representa la categoría elegida.
     */
    fun onCategorySelect(category: UiText) {
        _selectedCategory.value = category
    }
}
