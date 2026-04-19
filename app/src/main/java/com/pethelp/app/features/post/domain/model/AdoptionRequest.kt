package com.pethelp.app.features.post.domain.model

import com.pethelp.app.core.domain.model.PostStatus

data class AdoptionRequest(
    val id: String = "",
    val postId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",
    val message: String = "",
    val housingType: String = "",
    val hasOutdoorSpace: String = "",
    val hasExperience: String = "",
    val phone: String = "",
    val contactPreference: String = "",
    val status: AdoptionRequestStatus = AdoptionRequestStatus.PENDING,
    val postStatus: PostStatus? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AdoptionRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
