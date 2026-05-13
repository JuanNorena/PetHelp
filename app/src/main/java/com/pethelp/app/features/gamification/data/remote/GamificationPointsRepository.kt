/**
 * Repositorio remoto para sincronizar puntos de gamificación con Firestore.
 *
 * Lee y actualiza el documento de puntos del usuario en la colección
 * `users` de Firestore, manteniendo el total acumulado sincronizado
 * entre dispositivos.
 */
package com.pethelp.app.features.gamification.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.domain.model.UserLevel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
 * Repositorio responsable de actualizar los puntos y nivel del usuario en Firestore.
 *
 * **Responsabilidad Principal:**
 * Recibe deltas de puntos provenientes del [GamificationEngine] y los aplica
 * de forma atómica sobre el documento del usuario en Firestore usando una transacción.
 * Además, recalcula el nivel del usuario basándose en el nuevo total de puntos.
 *
 * **Por qué usar una transacción:**
 * Garantiza que la lectura de `points` y la escritura del nuevo valor ocurran
 * de forma atómica, evitando race conditions si el usuario completa dos misiones
 * casi simultáneamente desde diferentes hilos.
 *
 * @param firestore Instancia de Firebase Firestore para acceder a la colección de usuarios.
 * @param firebaseAuth Instancia de Firebase Auth para obtener el UID del usuario actual.
 */
@Singleton
class GamificationPointsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    /** Referencia a la colección "users" de Firestore. */
    private val usersCollection = firestore.collection(Constants.COLLECTION_USERS)

    /**
     * Suma (o resta) puntos al usuario autenticado y actualiza su nivel.
     *
     * **Flujo:**
     * 1. Obtiene el UID del usuario actual; si no hay sesión, sale sin hacer nada.
     * 2. Si `delta` es cero, no ejecuta la transacción para evitar escrituras innecesarias.
     * 3. Dentro de una transacción Firestore:
     *    - Lee el valor actual de `points` del documento del usuario.
     *    - Calcula el nuevo total asegurando que no sea negativo.
     *    - Determina el nivel correspondiente al nuevo puntaje.
     *    - Escribe `points` y `level` actualizados.
     *
     * @param delta Cantidad de puntos a agregar (positivo) o restar (negativo).
     */
    suspend fun addPoints(delta: Int) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        if (delta == 0) return

        firestore.runTransaction { transaction ->
            val userRef = usersCollection.document(userId)
            val snapshot = transaction.get(userRef)
            // Lee los puntos actuales del usuario; usa 0 como fallback si el campo no existe.
            val currentPoints = snapshot.getLong("points")?.toInt() ?: 0
            val updatedPoints = (currentPoints + delta).coerceAtLeast(0)
            val updatedLevel = calculateUserLevel(updatedPoints)
            transaction.update(
                userRef,
                mapOf(
                    "points" to updatedPoints,
                    "level" to updatedLevel.name
                )
            )
        }.await()
    }

    /**
     * Determina el nivel del usuario según su puntaje total.
     *
     * Ordena los niveles de mayor a menor según `minPoints` y selecciona el
     * primero cuyo umbral sea alcanzado o superado por el puntaje dado.
     *
     * @param points Puntaje total actual del usuario.
     * @return El nivel más alto cuyo requisito de puntos se cumple.
     */
    private fun calculateUserLevel(points: Int): UserLevel {
        return UserLevel.values()
            .sortedByDescending { it.minPoints }
            .first { points >= it.minPoints }
    }
}
