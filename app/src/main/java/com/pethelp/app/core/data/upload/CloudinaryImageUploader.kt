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
 * Implementación de ImageUploader que sube imágenes a Cloudinary.
 *
 * Esta clase se encarga de:
 * 1. Inicializar el SDK de Cloudinary (MediaManager) una sola vez.
 * 2. Convertir URIs locales en rutas de archivo seguras para carga.
 * 3. Subir la imagen usando el preset de carga no firmado.
 * 4. Devolver la URL pública generada por Cloudinary.
 *
 * Se usa típicamente desde un ViewModel cuando el usuario selecciona una
 * imagen y se necesita enviarla a la nube antes de guardar un post.
 */
@Singleton
class CloudinaryImageUploader @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageUploader {

    companion object {
        /**
         * Bandera global para inicializar MediaManager solo una vez.
         *
         * MediaManager es un SDK con estado interno y no debe inicializarse
         * varias veces en una misma aplicación.
         */
        private val isInitialized = AtomicBoolean(false)
    }

    private data class PreparedUploadSource(
        val source: String,
        val tempFile: File? = null
    )

    /**
     * Asegura que Cloudinary esté inicializado antes de usarlo.
     *
     * Verifica las variables de configuración necesarias de BuildConfig y
     * llama a MediaManager.init(...) una sola vez.
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
     * Sube una imagen local a Cloudinary y devuelve la URL final.
     *
     * El parámetro `localUri` puede ser una URI de contenido (`content://`) o
     * una ruta de archivo (`file://` o ruta absoluta). La función convierte esta
     * referencia en un origen seguro antes de subirla.
     *
     * @param localUri URI local de la imagen.
     * @param folder carpeta dentro de Cloudinary donde se guardará la imagen.
     * @return URL pública segura de la imagen subida.
     * @throws IllegalStateException si falta configuración o si la subida falla.
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
     * Prepara la fuente de imagen para la carga a Cloudinary.
     *
     * Cloudinary necesita una ruta de archivo válida. Si la URI proviene del
     * selector de fotos, puede ser un `content://` que caduca o pierde permiso.
     * En ese caso copiamos el contenido a un archivo temporal en el caché.
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

    /**
     * Copia el contenido de una URI de tipo `content://` a un archivo temporal.
     *
     * Esto evita problemas cuando el URI original deja de ser válido mientras se
     * realiza la subida asíncrona. El archivo temporal se elimina después de
     * completar la subida, tanto si tiene éxito como si falla.
     */
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
