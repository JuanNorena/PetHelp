package com.pethelp.app.features.ai.domain.repository

import com.google.gson.annotations.SerializedName
import com.pethelp.app.core.domain.model.PostCategory

data class AiMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    @SerializedName("reasoning_details")
    val reasoningDetails: List<ReasoningDetail>? = null
)

data class ReasoningDetail(
    val type: String? = null,
    val id: String? = null,
    val format: String? = null,
    val index: Int? = null,
    val text: String? = null,
    val summary: String? = null,
    val data: String? = null,
    val signature: String? = null
)

data class AiChatRequest(
    val model: String = "gemini-2.5-flash-lite",
    val messages: List<AiMessage>,
    val reasoning: Map<String, Boolean> = mapOf("enabled" to true),
    val temperature: Double = 0.35,
    @SerializedName("max_tokens")
    val maxTokens: Int? = 900
)

data class AiChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int? = null,
    val message: AiMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)

data class AiCategorySuggestion(
    val category: PostCategory,
    val confidence: Int,
    val reason: String
)

interface AiChatRepository {
    suspend fun callGemini(request: AiChatRequest): Result<AiChatResponse>
    suspend fun getRecommendedPetsBasedOnQuiz(answers: Map<String, String>): Result<String>
    suspend fun suggestPostCategory(
        title: String,
        description: String,
        animalType: String
    ): Result<AiCategorySuggestion>
}
