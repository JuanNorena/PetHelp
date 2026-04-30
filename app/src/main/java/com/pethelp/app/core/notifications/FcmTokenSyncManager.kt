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

@Singleton
class FcmTokenSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) {

    private val prefs by lazy {
        context.getSharedPreferences(Constants.FCM_PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun syncPendingAndCurrentToken() {
        val currentToken = runCatching { firebaseMessaging.token.await() }.getOrNull()
        if (!currentToken.isNullOrBlank()) {
            saveTokenForCurrentUserOrQueue(currentToken)
        }

        val pendingToken = prefs.getString(Constants.FCM_PENDING_TOKEN_KEY, null)
        if (!pendingToken.isNullOrBlank()) {
            saveTokenForCurrentUserOrQueue(pendingToken)
        }
    }

    suspend fun handleNewToken(token: String) {
        saveTokenForCurrentUserOrQueue(token)
    }

    private suspend fun saveTokenForCurrentUserOrQueue(token: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            prefs.edit().putString(Constants.FCM_PENDING_TOKEN_KEY, token).apply()
            return
        }

        val tokenDocId = tokenDocId(token)

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

        prefs.edit().remove(Constants.FCM_PENDING_TOKEN_KEY).apply()
    }

    private fun tokenDocId(token: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
