/**
 * Administrador de sincronización de tokens FCM (Firebase Cloud Messaging).
 *
 * Registra el token FCM del dispositivo en Firestore bajo el documento
 * del usuario autenticado, permitiendo enviar notificaciones push
 * dirigidas a dispositivos específicos.
 */
package com.pethelp.app.core.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.pethelp.app.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de sincronización de tokens FCM (Firebase Cloud Messaging).
 *
 * **Responsabilidad Principal:**
 * Asegurar que el token FCM del dispositivo se registre correctamente en Firestore
 * bajo el usuario autenticado, permitiendo que el backend envíe notificaciones push
 * dirigidas a este dispositivo.
 *
 * **Flujo de Trabajo:**
 * 1. Al iniciar la app, se obtiene el token actual de Firebase Messaging.
 * 2. Si hay un usuario autenticado, el token se guarda en Firestore dentro de la
 *    subcolección `fcmTokens` del documento del usuario.
 * 3. Si no hay usuario autenticado, el token se encola en SharedPreferences para
 *    sincronizarlo en cuanto el usuario inicie sesión.
 * 4. Al cerrar sesión, todos los tokens activos del usuario se marcan como
 *    `enabled = false` para evitar notificaciones a un dispositivo sin sesión.
 *
 * **Notas de Implementación:**
 * - El ID del documento en Firestore se genera como un hash SHA-256 del token para
 *   evitar duplicados y permitir actualizaciones idempotentes.
 * - Se usa `chunked(400)` al deshabilitar tokens para no exceder el límite de 500
 *   operaciones por batch de Firestore.
 */
@Singleton
class FcmTokenSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) {

    /**
     * Referencia a SharedPreferences donde se encolan tokens pendientes.
     *
     * Se usa `lazy` para evitar la creación del objeto hasta que realmente se necesite,
     * optimizando el arranque de la aplicación.
     */
    private val prefs by lazy {
        context.getSharedPreferences(Constants.FCM_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Sincroniza tanto el token FCM actual como cualquier token pendiente.
     *
     * Este método se llama típicamente al iniciar la aplicación o cuando se detecta
     * un cambio de estado de autenticación. Primero intenta registrar el token activo
     * del dispositivo; luego procesa cualquier token que quedó pendiente por falta
     * de sesión en ejecuciones anteriores.
     */
    suspend fun syncPendingAndCurrentToken() {
        // Obtiene el token actual de Firebase Messaging de forma segura.
        val currentToken = runCatching { firebaseMessaging.token.await() }.getOrNull()
        if (!currentToken.isNullOrBlank()) {
            saveTokenForCurrentUserOrQueue(currentToken)
        }

        // Procesa tokens pendientes que se guardaron mientras no había sesión.
        val pendingToken = prefs.getString(Constants.FCM_PENDING_TOKEN_KEY, null)
        if (!pendingToken.isNullOrBlank()) {
            saveTokenForCurrentUserOrQueue(pendingToken)
        }
    }

    /**
     * Maneja un nuevo token generado por Firebase (por ejemplo, tras una
     * reinstalación o rotación de token).
     *
     * @param token El nuevo token FCM recibido del servicio de Firebase.
     */
    suspend fun handleNewToken(token: String) {
        saveTokenForCurrentUserOrQueue(token)
    }

    /**
     * Desactiva todos los tokens FCM del usuario actual en Firestore.
     *
     * Se utiliza al cerrar sesión para garantizar que el dispositivo deje de recibir
     * notificaciones push dirigidas al usuario que ya no está autenticado.
     *
     * **Proceso:**
     * 1. Elimina cualquier token pendiente de SharedPreferences.
     * 2. Consulta todos los tokens activos del usuario en Firestore.
     * 3. Actualiza cada token a `enabled = false` usando batches de Firestore.
     */
    suspend fun disableTokensForCurrentUser() {
        val uid = auth.currentUser?.uid
        prefs.edit().remove(Constants.FCM_PENDING_TOKEN_KEY).apply()
        if (uid.isNullOrBlank()) return

        val userRef = firestore.collection(Constants.COLLECTION_USERS).document(uid)
        val activeTokens = userRef
            .collection(Constants.COLLECTION_FCM_TOKENS)
            .whereEqualTo("enabled", true)
            .get()
            .await()

        if (activeTokens.isEmpty) return

        // Firestore tiene un límite de 500 operaciones por batch.
        // Se usa chunked(400) para dejar margen de seguridad.
        activeTokens.documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "enabled" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }
            batch.commit().await()
        }
    }

    /**
     * Guarda un token FCM en Firestore si hay sesión activa, o lo encola en
     * SharedPreferences si no la hay.
     *
     * @param token El token FCM a registrar.
     */
    private suspend fun saveTokenForCurrentUserOrQueue(token: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            // No hay sesión: encola el token para sincronizarlo más tarde.
            prefs.edit().putString(Constants.FCM_PENDING_TOKEN_KEY, token).apply()
            return
        }

        val tokenDocId = tokenDocId(token)

        // Guarda o actualiza el token en la subcolección fcmTokens del usuario.
        firestore.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .collection(Constants.COLLECTION_FCM_TOKENS)
            .document(tokenDocId)
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "enabled" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()

        // Limpia el token pendiente ya que ahora está sincronizado.
        prefs.edit().remove(Constants.FCM_PENDING_TOKEN_KEY).apply()
    }

    /**
     * Genera un identificador determinista para un token FCM.
     *
     * Usa SHA-256 para convertir el token largo en un hash hexadecimal de 64
     * caracteres que puede usarse como ID de documento de Firestore. Esto evita
     * duplicados y hace que las actualizaciones sean idempotentes.
     *
     * @param token El token FCM original.
     * @return Un hash SHA-256 en formato hexadecimal.
     */
    private fun tokenDocId(token: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
