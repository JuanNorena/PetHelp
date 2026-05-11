package com.pethelp.app.features.gamification.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pethelp.app.core.common.Constants
import com.pethelp.app.core.domain.model.UserLevel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class GamificationPointsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val usersCollection = firestore.collection(Constants.COLLECTION_USERS)

    suspend fun addPoints(delta: Int) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        if (delta == 0) return

        firestore.runTransaction { transaction ->
            val userRef = usersCollection.document(userId)
            val snapshot = transaction.get(userRef)
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

    private fun calculateUserLevel(points: Int): UserLevel {
        return UserLevel.values()
            .sortedByDescending { it.minPoints }
            .first { points >= it.minPoints }
    }
}
