package com.pethelp.app.features.ai.domain.repository

data class AiMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val reasoningDetails: List<ReasoningDetail>? = null
)

data class ReasoningDetail(
    val index: Int,
    val type: String, // "reasoning.text"
    val text: String,
    val format: String
)

data class AiChatRequest(
    val model: String = "google/gemma-4-31b-it:free",
    val messages: List<AiMessage>,
    val reasoning: Map<String, Boolean> = mapOf("enabled" to true)
)

data class AiChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val message: AiMessage?,
    val finishReason: String?
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

interface AiChatRepository {
    suspend fun callOpenRouter(request: AiChatRequest): Result<AiChatResponse>
    suspend fun getRecommendedPetsBasedOnQuiz(answers: Map<String, String>): Result<String>
}
