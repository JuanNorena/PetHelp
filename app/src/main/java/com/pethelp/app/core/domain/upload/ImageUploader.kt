/**
 * Contrato de subida de imágenes en la capa de dominio.
 *
 * Define la operación mínima necesaria para subir una imagen,
 * desacoplada del proveedor concreto (Cloudinary, S3, etc.).
 */
package com.pethelp.app.core.domain.upload

/**
 * Contrato de subida de imágenes usado en la capa de dominio.
 *
 * Esta interfaz define lo mínimo que necesita la aplicación para subir una
 * imagen sin depender de detalles concretos del proveedor.
 *
 * Implementaciones típicas pueden ser:
 * - CloudinaryImageUploader
 * - FirebaseStorageImageUploader
 * - S3ImageUploader
 *
 * Al inyectar `ImageUploader` en un ViewModel, la UI no necesita conocer ni
 * una sola clase concreta de subida, lo que facilita pruebas y mantenimiento.
 *
 * Ejemplo de uso:
 * ```kotlin
 * class CreatePostViewModel @Inject constructor(
 *     private val imageUploader: ImageUploader
 * ) : ViewModel() {
 *     suspend fun upload(uri: String) {
 *         val url = imageUploader.uploadImage(uri, "pethelp/posts")
 *         // Guardar URL en la publicación
 *     }
 * }
 * ```
 */
interface ImageUploader {

    /**
     * Sube una imagen local a un proveedor externo y devuelve su URL pública.
     *
     * @param localUri URI local de la imagen, por ejemplo `content://...` o
     *        una ruta de archivo. La implementación decide cómo manejarla.
     * @param folder Carpeta o ruta dentro del proveedor donde se guardará la imagen.
     * @return URL pública accesible de la imagen subida.
     * @throws Exception si ocurre un error de red, permisos o configuración.
     */
    suspend fun uploadImage(localUri: String, folder: String): String
}
