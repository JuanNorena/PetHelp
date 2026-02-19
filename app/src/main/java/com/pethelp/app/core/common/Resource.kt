package com.pethelp.app.core.common

/**
 * Clase sellada genérica para representar el estado de cualquier
 * operación asíncrona (llamada a Firebase, API, Room, etc.).
 *
 * Uso típico en ViewModel:
 *   private val _uiState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
 *
 * Uso en UI (Compose):
 *   when (val state = uiState.collectAsStateWithLifecycle().value) {
 *       is Resource.Loading -> CircularProgressIndicator()
 *       is Resource.Success -> PostList(state.data)
 *       is Resource.Error   -> ErrorMessage(state.message)
 *   }
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T)               : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null)       : Resource<T>(data)
}
