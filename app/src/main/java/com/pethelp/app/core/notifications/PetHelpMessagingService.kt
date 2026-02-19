package com.pethelp.app.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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

    /**
     * Se llama cuando llega un mensaje push mientras la app está en primer plano
     * o cuando el mensaje tiene un payload de "data" (no solo "notification").
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // TODO (Fase 3): mostrar notificación local con NotificationManager
        //   - Filtrar por distancia geográfica antes de mostrar
        //   - Guardar notificación en Firestore/Room para la lista
    }

    /**
     * Se llama cuando Firebase genera un nuevo token para el dispositivo.
     * Guardar el token en Firestore para poder enviar notificaciones dirigidas.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO (Fase 3): guardar token en Firestore del usuario autenticado
    }
}
