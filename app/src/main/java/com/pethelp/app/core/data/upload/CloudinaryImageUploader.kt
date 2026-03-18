package com.pethelp.app.core.data.upload

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.pethelp.app.BuildConfig
import com.pethelp.app.core.domain.upload.ImageUploader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
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

        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get()
                .upload(localUri)
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
                            continuation.resumeWithException(
                                IllegalStateException("Cloudinary no retornó URL de la imagen.")
                            )
                            return
                        }
                        continuation.resume(finalUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resumeWithException(
                            IllegalStateException(
                                error?.description ?: "Error desconocido al subir imagen a Cloudinary."
                            )
                        )
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
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
            }
        }
    }
}
