package com.pethelp.app.core.common

/**
 * Clase sellada genérica para representar el estado de cualquier
 * operación asíncrona (llamada a Firebase, API, Room, etc.).
 */
sealed class Resource<T>(
    val data: T? = null,
    val uiText: UiText? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(uiText: UiText, data: T? = null) : Resource<T>(data, uiText)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
