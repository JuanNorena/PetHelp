package com.pethelp.app.core.domain.upload

/**
 * Interfaz que define el contrato para subir imágenes a un proveedor externo.
 *
 * La idea es mantener la lógica de subida **desacoplada** de la UI y de los
 * ViewModels, para que sea fácil cambiar de proveedor (Cloudinary, S3, Firebase,
 * etc.) sin modificar el código de las pantallas.
 *
 * Ejemplo de uso:
 * ```kotlin
 * class CreatePostViewModel @Inject constructor(
 *     private val imageUploader: ImageUploader
 * ) : ViewModel() {
 *     suspend fun upload(uri: String) {
 *         val url = imageUploader.uploadImage(uri, "pethelp/posts")
 *     }
 * }
 * ```
 */
interface ImageUploader {

    /**
     * Sube una imagen local identificada por su URI y devuelve la URL pública.
     *
     * @param localUri URI local de la imagen (p. ej. `content://...`).
     * @param folder Carpeta dentro del proveedor (p. ej. `pethelp/posts`).
     * @return URL pública de la imagen subida.
     * @throws Exception si la subida falla o falta configuración.
     */
    suspend fun uploadImage(localUri: String, folder: String): String
}
