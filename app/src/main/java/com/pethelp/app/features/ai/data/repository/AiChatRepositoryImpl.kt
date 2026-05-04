package com.pethelp.app.features.ai.data.repository

import com.google.gson.Gson
import com.pethelp.app.BuildConfig
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import com.pethelp.app.features.ai.domain.repository.AiChatResponse
import com.pethelp.app.features.ai.domain.repository.AiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AiChatRepositoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : AiChatRepository {

    companion object {
        private const val OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"
    }

    override suspend fun callOpenRouter(request: AiChatRequest): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.OPEN_ROUTER_API_KEY
                if (apiKey.isEmpty()) {
                    return@withContext Result.failure(Exception("OPEN_ROUTER_API_KEY not configured"))
                }

                // Serializar request a JSON
                val requestBody = gson.toJson(request).toRequestBody("application/json".toMediaType())

                // Construir petición HTTP a OpenRouter directamente
                val httpRequest = Request.Builder()
                    .url(OPENROUTER_API_URL)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                // Ejecutar llamada
                val response = httpClient.newCall(httpRequest).execute()
                val bodyString = response.body?.string() ?: "{}"

                // Parsear respuesta
                return@withContext if (response.isSuccessful) {
                    val parsedResponse = gson.fromJson(bodyString, AiChatResponse::class.java)
                    Result.success(parsedResponse)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $bodyString"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getRecommendedPetsBasedOnQuiz(answers: Map<String, String>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Construir prompt para IA basado en respuestas del cuestionario
                val prompt = buildQuizPrompt(answers)

                // Llamar a OpenRouter
                val request = AiChatRequest(
                    messages = listOf(
                        AiMessage(role = "user", content = prompt)
                    ),
                    reasoning = mapOf("enabled" to true)
                )

                val result = callOpenRouter(request)

                return@withContext if (result.isSuccess) {
                    val response = result.getOrNull()
                    val assistantMessage = response?.choices?.firstOrNull()?.message?.content ?: ""
                    Result.success(assistantMessage)
                } else {
                    result as Result<String>
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildQuizPrompt(answers: Map<String, String>): String {
        return """
        Basado en las siguientes preferencias del usuario sobre mascotas, proporciona una lista de mascotas recomendadas con una breve explicación:
        
        Preferencias:
        ${answers.entries.joinToString("\n") { (key, value) -> "- $key: $value" }}
        
        Por favor, recomienda 3-5 mascotas que sean ideales para este usuario, considerando:
        1. Compatibilidad con su estilo de vida y nivel de experiencia
        2. Requerimientos de espacio y tiempo disponible
        3. Características de comportamiento y cuidados necesarios
        
        Formatea la respuesta como una lista numerada con el nombre de la mascota y una breve justificación.
        """.trimIndent()
    }
}
