package com.pethelp.app.core.common

/**
 * Representa el estado de una operación asíncrona en la aplicación.
 *
 * Esta clase sellada se usa para modelar resultados que pueden estar en tres
 * estados principales:
 *   - Loading: la operación está en curso.
 *   - Success: la operación terminó bien y hay datos disponibles.
 *   - Error: la operación falló y puede tener un mensaje de error.
 *
 * Es un patrón común en arquitecturas basadas en ViewModel y StateFlow, porque
 * permite que la UI observe el estado completo de la operación y reaccione
 * según el caso.
 *
 * Ejemplo de uso en un ViewModel:
 *   private val _uiState = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
 *
 * Ejemplo de uso en Compose:
 *   when (val state = uiState.collectAsStateWithLifecycle().value) {
 *       is Resource.Loading -> CircularProgressIndicator()
 *       is Resource.Success -> PostList(state.data)
 *       is Resource.Error -> ErrorMessage(state.uiText)
 *   }
 */
sealed class Resource<T>(
    /**
     * Datos devueltos por la operación cuando el estado es Success.
     * Puede ser null mientras la operación está cargando o cuando ocurre un error.
     */
    val data: T? = null,
    

    /**
     * Mensaje descriptivo en caso de error.
     * En una operación exitosa normalmente será null.
     */
    val message: String? = null
) {

    /** Alias de compatibilidad para la capa de presentacion. */
    val uiText: UiText?
        get() = message

    /**
     * Resultado exitoso con datos disponibles.
     *
     * @param data datos de la operación que se pueden mostrar en pantalla.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Resultado con error.
     *
     * @param uiText texto localizable con la descripción del fallo.
     * @param data datos opcionales que aún pueden estar disponibles.
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Estado de carga de la operación.
     *
     * @param data datos opcionales que pueden mostrarse mientras se carga.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

/**
 * Compatibilidad para la capa de presentacion: transforma el mensaje de error
 * crudo de [Resource.Error] en [UiText] para poder mostrarlo en snackbar/UI.
 */
val Resource<*>.uiText: UiText?
    get() = when (this) {
        is Resource.Error -> message
        else -> null
    }

