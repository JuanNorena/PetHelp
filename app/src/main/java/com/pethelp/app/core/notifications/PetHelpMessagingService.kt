/**
 * Servicio de Firebase Cloud Messaging (FCM) de PetHelp.
 *
 * Procesa mensajes push en primer y segundo plano, crea canales de notificación
 * en Android 8+ y muestra notificaciones locales cuando llegan mensajes nuevos.
 * También renueva y guarda el token FCM cuando cambia.
 */
package com.pethelp.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pethelp.app.MainActivity
import com.pethelp.app.R
import com.pethelp.app.core.common.Constants
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de Firebase Cloud Messaging.
 *
 * Se encarga de:
 * 1. Procesar mensajes push en primer y segundo plano.
 * 2. Renovar y guardar el token FCM cuando cambia.
 *
 * Debe estar registrado en AndroidManifest.xml con el intent-filter
 * com.google.firebase.MESSAGING_EVENT.
 */
class PetHelpMessagingService : FirebaseMessagingService() {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MessagingServiceEntryPoint {
        fun fcmTokenSyncManager(): FcmTokenSyncManager
    }

    private fun tokenManager(): FcmTokenSyncManager {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MessagingServiceEntryPoint::class.java
        )
        return entryPoint.fcmTokenSyncManager()
    }

    /**
     * Se llama cuando llega un mensaje push mientras la app está en primer plano
     * o cuando el mensaje tiene un payload de "data" (no solo "notification").
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        createNotificationChannelIfNeeded()

        // Si el usuario denegó permiso en Android 13+, no mostramos push local,
        // pero la notificación in-app seguirá llegando por Firestore.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: getString(R.string.notifications_title)

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: getString(R.string.notifications_push_default_body)

        val relatedPostId = remoteMessage.data["relatedPostId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!relatedPostId.isNullOrBlank()) {
                putExtra(Constants.NOTIFICATION_INTENT_POST_ID, relatedPostId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    /**
     * Se llama cuando Firebase genera un nuevo token para el dispositivo.
     * Guardar el token en Firestore para poder enviar notificaciones dirigidas.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ioScope.launch {
            tokenManager().handleNewToken(token)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Asegura tener token actual incluso si onNewToken no se dispara al inicio.
        ioScope.launch {
            runCatching { tokenManager().syncPendingAndCurrentToken() }
        }
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notifications_channel_description)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
