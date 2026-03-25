package com.pethelp.app.core.data.upload

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.pethelp.app.BuildConfig
import com.pethelp.app.core.domain.upload.ImageUploader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación de **ImageUploader** usando **Cloudinary**.
 *
 * Esta clase se encarga de inicializar el SDK de Cloudinary (MediaManager)
 * y subir imágenes locales (URI) al servicio.
 *
 * Se usa el preset de carga (unsigned) configurado en **BuildConfig.CLOUDINARY_UPLOAD_PRESET**
 * y el nombre de la nube en **BuildConfig.CLOUDINARY_CLOUD_NAME**.
 *
 * El método `uploadImage()` devuelve la URL segura (https) que Cloudinary genera
 * para la imagen subida, lista para guardarse en Firestore.
 *
 * Este componente está diseñado para usarse desde un ViewModel (por ejemplo,
 * `CreatePostViewModel`) donde se hace el flujo de UI -> validaciones -> carga -> persistencia.
 */
@Singleton
class CloudinaryImageUploader @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageUploader {

    companion object {
        private val isInitialized = AtomicBoolean(false)
    }

    private data class PreparedUploadSource(
        val source: String,
        val tempFile: File? = null
    )

    /**
     * Inicializa el SDK de Cloudinary (MediaManager) una sola vez por aplicación.
     *
     * Requiere que las constantes del build config estén definidas:
     * - CLOUDINARY_CLOUD_NAME: el nombre de la nube en Cloudinary.
     *
     * Lanza una excepción clara si falta la configuración.
     */
    private fun ensureInitialized() {
        if (!isInitialized.get()) {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            if (cloudName.isBlank()) {
                throw IllegalStateException("Cloudinary no está configurado. Falta CLOUDINARY_CLOUD_NAME.")
            }

            val config = hashMapOf(
                "cloud_name" to cloudName,
                "secure" to true
            )
            MediaManager.init(context, config)
            isInitialized.set(true)
        }
    }

    /**
     * Sube una imagen local a Cloudinary y devuelve la URL pública.
     *
     * @param localUri URI local (por ejemplo, `content://...`) de la imagen seleccionada.
     * @param folder Carpeta dentro de Cloudinary donde se guardará la imagen (ej. "pethelp/posts").
     * @return URL de la imagen subida (usualmente `secure_url`).
     * @throws IllegalStateException si falta configuración o la subida falla.
     */
    override suspend fun uploadImage(localUri: String, folder: String): String {
        ensureInitialized()

        val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        if (uploadPreset.isBlank()) {
            throw IllegalStateException("Cloudinary no está configurado. Falta CLOUDINARY_UPLOAD_PRESET.")
        }

        val preparedSource = withContext(Dispatchers.IO) {
            prepareUploadSource(localUri)
        }

        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get()
                .upload(preparedSource.source)
                .unsigned(uploadPreset)
                .option("folder", folder)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) = Unit

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) = Unit

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val secureUrl = resultData?.get("secure_url") as? String
                        val fallbackUrl = resultData?.get("url") as? String
                        val finalUrl = secureUrl ?: fallbackUrl

                        if (finalUrl.isNullOrBlank()) {
                            preparedSource.tempFile?.delete()
                            continuation.resumeWithException(
                                IllegalStateException("Cloudinary no retornó URL de la imagen.")
                            )
                            return
                        }
                        preparedSource.tempFile?.delete()
                        continuation.resume(finalUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        preparedSource.tempFile?.delete()
                        continuation.resumeWithException(
                            IllegalStateException(
                                error?.description ?: "Error desconocido al subir imagen a Cloudinary."
                            )
                        )
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        preparedSource.tempFile?.delete()
                        continuation.resumeWithException(
                            IllegalStateException(
                                error?.description ?: "La subida fue reprogramada por Cloudinary."
                            )
                        )
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
                preparedSource.tempFile?.delete()
            }
        }
    }

    /**
     * Convierte una referencia local de imagen en una fuente estable para Cloudinary.
     *
     * Photo Picker entrega URIs del tipo `content://media/picker/...` que pueden
     * caducar o perder permisos al ejecutarse la subida asíncrona. Para evitar
     * errores "does not exist", copiamos esos contenidos a un archivo temporal
     * propio en cache y subimos ese archivo.
     */
    private fun prepareUploadSource(localUri: String): PreparedUploadSource {
        if (localUri.isBlank()) {
            throw IllegalStateException("Selecciona una imagen válida.")
        }

        val parsedUri = Uri.parse(localUri)
        return when (parsedUri.scheme?.lowercase()) {
            "content" -> {
                val tempFile = copyContentUriToTempFile(parsedUri)
                PreparedUploadSource(source = tempFile.absolutePath, tempFile = tempFile)
            }
            "file" -> {
                val path = parsedUri.path
                if (path.isNullOrBlank()) {
                    throw IllegalStateException("La ruta de archivo seleccionada no es válida.")
                }

                val file = File(path)
                if (!file.exists()) {
                    throw IllegalStateException("El archivo seleccionado no existe.")
                }

                PreparedUploadSource(source = file.absolutePath)
            }
            else -> {
                val file = File(localUri)
                if (file.exists()) {
                    PreparedUploadSource(source = file.absolutePath)
                } else {
                    throw IllegalStateException(
                        "No se pudo acceder a la imagen seleccionada. Intenta seleccionarla nuevamente."
                    )
                }
            }
        }
    }

    private fun copyContentUriToTempFile(uri: Uri): File {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"

        val uploadsDir = File(context.cacheDir, "cloudinary_uploads").apply {
            if (!exists()) mkdirs()
        }

        val tempFile = File.createTempFile("upload_", ".${extension}", uploadsDir)

        resolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo leer la imagen seleccionada.")

        return tempFile
    }
}
