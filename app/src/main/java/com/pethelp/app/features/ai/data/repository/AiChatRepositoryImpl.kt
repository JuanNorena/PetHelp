package com.pethelp.app.features.ai.data.repository

import com.google.gson.Gson
import com.pethelp.app.BuildConfig
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.features.ai.domain.repository.AiCategorySuggestion
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import com.pethelp.app.features.ai.domain.repository.AiChatResponse
import com.pethelp.app.features.ai.domain.repository.AiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AiChatRepositoryImpl @Inject constructor(
    private val httpClient: okhttp3.OkHttpClient,
    private val gson: Gson
) : AiChatRepository {

    companion object {
        private const val OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"
    }

    override suspend fun callOpenRouter(request: AiChatRequest): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.OPEN_ROUTER_API_KEY
            val proxyUrl = BuildConfig.OPEN_ROUTER_PROXY_URL
            if (apiKey.isEmpty() && proxyUrl.isEmpty()) {
                return@withContext Result.failure(Exception("OPEN_ROUTER_API_KEY not configured"))
            }

            val requestBody = gson.toJson(request).toRequestBody("application/json".toMediaType())
            val url = proxyUrl.ifBlank { OPENROUTER_API_URL }

            val maxRetries = 3
            var attempt = 0
            var delayMs = 500L

            suspend fun executeWithRetry(): Result<AiChatResponse> {
                while (true) {
                    try {
                        val requestBuilder = Request.Builder()
                            .url(url)
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("HTTP-Referer", "https://pethelp.app")
                            .addHeader("X-OpenRouter-Title", "PetHelp Android")
                            .post(requestBody)

                        if (proxyUrl.isBlank()) {
                            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                        }

                        val response = httpClient.newCall(requestBuilder.build()).execute()
                        val bodyString = response.body?.string() ?: "{}"

                        if (response.isSuccessful) {
                            val parsedResponse = gson.fromJson(bodyString, AiChatResponse::class.java)
                            return Result.success(parsedResponse)
                        }

                        val code = response.code
                        val msg = parseOpenRouterError(code, bodyString)
                        val shouldRetry = code == 429 || code in 500..599

                        val providerHint = runCatching {
                            val root = gson.fromJson(bodyString, Map::class.java)
                            val error = root["error"] as? Map<*, *>
                            val metadata = error?.get("metadata") as? Map<*, *>
                            val raw = metadata?.get("raw") as? String
                            val isByok = metadata?.get("is_byok") as? Boolean
                            when {
                                isByok == false && raw != null -> " Provider hint: $raw"
                                raw != null -> " Provider hint: $raw"
                                else -> ""
                            }
                        }.getOrDefault("")

                        if (shouldRetry && attempt < maxRetries) {
                            attempt++
                            val jitter = Random.nextLong(0, 200)
                            delay(delayMs + jitter)
                            delayMs = (delayMs * 2).coerceAtMost(5000L)
                            continue
                        }

                        return Result.failure(Exception("$msg$providerHint"))
                    } catch (e: Exception) {
                        if (attempt < maxRetries) {
                            attempt++
                            val jitter = Random.nextLong(0, 200)
                            delay(delayMs + jitter)
                            delayMs = (delayMs * 2).coerceAtMost(5000L)
                            continue
                        }
                        return Result.failure(e)
                    }
                }
            }

            return@withContext executeWithRetry()
        }
    }

    override suspend fun getRecommendedPetsBasedOnQuiz(answers: Map<String, String>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AiChatRequest(
                    messages = listOf(
                        AiMessage(
                            role = "system",
                            content = "Eres un asesor de adopcion responsable para PetHelp. Responde en espanol claro, amable y accionable."
                        ),
                        AiMessage(role = "user", content = buildQuizPrompt(answers))
                    ),
                    reasoning = mapOf("enabled" to true)
                )

                val result = callOpenRouter(request)
                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Error en OpenRouter")
                    )
                }

                val assistantMessage = result.getOrNull()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    .orEmpty()
                    .trim()

                if (assistantMessage.isBlank()) {
                    return@withContext Result.failure(Exception("La IA no devolvio recomendaciones."))
                } else {
                    return@withContext Result.success(assistantMessage)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override suspend fun suggestPostCategory(
        title: String,
        description: String,
        animalType: String
    ): Result<AiCategorySuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AiChatRequest(
                    messages = listOf(
                        AiMessage(
                            role = "system",
                            content = "Clasificas publicaciones de PetHelp. Devuelve solo una categoria valida, una confianza y una razon breve."
                        ),
                        AiMessage(role = "user", content = buildCategoryPrompt(title, description, animalType))
                    ),
                    reasoning = mapOf("enabled" to true),
                    temperature = 0.2,
                    maxTokens = 220
                )

                val result = callOpenRouter(request)
                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Error en OpenRouter")
                    )
                }

                val content = result.getOrNull()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    .orEmpty()

                return@withContext Result.success(parseCategorySuggestion(content))
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    private fun buildQuizPrompt(answers: Map<String, String>): String {
        return """
            Basado en las siguientes preferencias del usuario sobre mascotas, proporciona una lista de mascotas recomendadas con una breve explicacion:

            Preferencias:
            ${answers.entries.joinToString("\n") { (key, value) -> "- $key: $value" }}

            Recomienda 3-5 mascotas ideales para este usuario, considerando:
            1. Compatibilidad con su estilo de vida y nivel de experiencia.
            2. Requerimientos de espacio y tiempo disponible.
            3. Caracteristicas de comportamiento y cuidados necesarios.
            4. Riesgos o compromisos que debe conocer antes de adoptar.

            Formatea la respuesta como lista numerada con tipo de mascota y justificacion breve.
            Cierra con una recomendacion practica para buscar publicaciones compatibles dentro de PetHelp.
        """.trimIndent()
    }

    private fun buildCategoryPrompt(title: String, description: String, animalType: String): String {
        return """
            Analiza esta publicacion y sugiere la categoria correcta.

            Categorias validas:
            - ADOPTION: animal que busca hogar definitivo.
            - LOST: mascota perdida por su familia.
            - FOUND: animal encontrado y se busca su familia.
            - TEMP_HOME: hogar temporal, transito o cuidado temporal.
            - VET_EVENT: jornada veterinaria, vacuna, esterilizacion o evento de salud.

            Titulo: ${title.trim()}
            Descripcion: ${description.trim()}
            Tipo de animal: ${animalType.trim()}

            Responde exactamente con este formato:
            CATEGORY=<una categoria valida>
            CONFIDENCE=<numero de 0 a 100>
            REASON=<maximo 120 caracteres>
        """.trimIndent()
    }

    private fun parseCategorySuggestion(content: String): AiCategorySuggestion {
        val normalized = content.uppercase()
        val category = PostCategory.entries.firstOrNull { normalized.contains(it.name) }
            ?: PostCategory.ADOPTION
        val confidence = Regex("""CONFIDENCE\s*=\s*(\d{1,3})""", RegexOption.IGNORE_CASE)
            .find(content)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?.coerceIn(0, 100)
            ?: 70
        val reason = Regex("""REASON\s*=\s*(.+)""", RegexOption.IGNORE_CASE)
            .find(content)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.take(160)
            ?: "Sugerencia generada a partir del titulo y la descripcion."

        return AiCategorySuggestion(
            category = category,
            confidence = confidence,
            reason = reason
        )
    }

    private fun parseOpenRouterError(code: Int, body: String): String {
        return runCatching {
            val root = gson.fromJson(body, Map::class.java)
            val error = root["error"] as? Map<*, *>
            val message = error?.get("message") as? String
            if (code == 429) {
                "La IA esta temporalmente limitada por el proveedor. Intenta de nuevo en unos segundos."
            } else {
                "OpenRouter HTTP $code: ${message ?: body.take(240)}"
            }
        }.getOrElse {
            "OpenRouter HTTP $code: ${body.take(240)}"
        }
    }
}
