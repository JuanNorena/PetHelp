/**
 * Implementación de [AiChatRepository] que comunica con modelos de lenguaje.
 *
 * **Proveedores Soportados:**
 * 1. **Gemini (Firebase AI Logic):** Proveedor principal.
 * 2. **NVIDIA NIM (Fallback directo):** Si Gemini falla, realiza llamada HTTP directa.
 *
 * Las claves y URLs de NVIDIA se leen de `local.properties` via [BuildConfig].
 */
package com.pethelp.app.features.ai.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.gson.Gson
import com.pethelp.app.BuildConfig
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.features.ai.domain.repository.AiCategorySuggestion
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import com.pethelp.app.features.ai.domain.repository.AiChatResponse
import com.pethelp.app.features.ai.domain.repository.AiMessage
import com.pethelp.app.features.ai.domain.repository.Choice
import com.pethelp.app.features.ai.domain.repository.ModerationAiAnalysis
import com.pethelp.app.features.ai.domain.repository.ModerationRiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * Implementación de [AiChatRepository] que comunica con modelos de lenguaje.
 *
 * **Proveedores Soportados:**
 * 1. **Gemini (Firebase AI Logic):** Proveedor principal. Usa el SDK de Firebase AI
 *    para enviar mensajes al modelo generativo configurado en la consola de Firebase.
 * 2. **NVIDIA NIM (Fallback directo):** Si Gemini falla, se realiza una llamada HTTP
 *    directa a la API de NVIDIA usando [OkHttpClient]. Esto garantiza que el chat
 *    y las recomendaciones funcionen incluso si hay problemas con Firebase.
 *
 * **Configuración:**
 * Las claves y URLs de NVIDIA se leen de `local.properties` y se inyectan en
 * [BuildConfig] durante la compilación. No se incluyen claves reales en el código fuente.
 *
 * @param okHttpClient Cliente HTTP inyectado desde [NetworkModule] para las llamadas a NVIDIA.
 */
class AiChatRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : AiChatRepository {

    /** Instancia de Gson para serializar/deserializar JSON en las llamadas a NVIDIA. */
    private val gson = Gson()

    /** Media type para peticiones JSON a la API de NVIDIA. */
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Envía una petición de chat al modelo de lenguaje.
     *
     * **Flujo:**
     * 1. Intenta llamar a Gemini (Firebase AI Logic).
     * 2. Si Gemini falla, automáticamente hace fallback a NVIDIA NIM.
     * 3. Normaliza los mensajes para que sean compatibles con ambos proveedores.
     *
     * @param request Petición con historial de mensajes y configuración del modelo.
     * @return Resultado con la respuesta del modelo o un error descriptivo.
     */
    override suspend fun callGemini(request: AiChatRequest): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            if (request.messages.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Empty AI request"))
            }

            val geminiResult = try {
                val ai = Firebase.ai
                val modelName = request.model.ifBlank { "gemini-2.5-flash-lite" }
                val model = ai.generativeModel(modelName)

                val normalized = request.messages.map { normalizeMessage(it) }
                val last = normalized.last()
                val history = normalized.dropLast(1).map { message ->
                    content(normalizeRole(message.role)) { text(message.content) }
                }

                val response = if (history.isNotEmpty()) {
                    val chat = model.startChat(history = history)
                    chat.sendMessage(last.content)
                } else {
                    model.generateContent(last.content)
                }

                val text = response.text.orEmpty().trim()
                if (text.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Empty AI response"))
                }

                val assistantMessage = AiMessage(role = "assistant", content = text)
                val wrapper = AiChatResponse(
                    id = null,
                    choices = listOf(
                        Choice(index = 0, message = assistantMessage, finishReason = "stop")
                    ),
                    usage = null
                )
                Result.success(wrapper)
            } catch (e: Exception) {
                Result.failure(e)
            }

            if (geminiResult.isSuccess) {
                geminiResult
            } else {
                callNvidia(request)
            }
        }
    }

    /**
     * Fallback directo a NVIDIA NIM usando OkHttp.
     *
     * Se ejecuta cuando Gemini falla. Lee la API key, modelo y URL base
     * desde [BuildConfig] y realiza una petición HTTP POST al endpoint
     * `/chat/completions` de NVIDIA.
     *
     * @param request Petición original que se reenvía a NVIDIA.
     * @return Resultado con la respuesta del modelo NVIDIA o error de configuración.
     */
    private suspend fun callNvidia(request: AiChatRequest): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.NVIDIA_API_KEY.trim()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("NVIDIA_API_KEY no esta configurada en local.properties.")
                )
            }

            try {
                val model = BuildConfig.NVIDIA_MODEL.ifBlank {
                    "meta/llama-4-maverick-17b-128e-instruct"
                }
                val endpoint = BuildConfig.NVIDIA_BASE_URL.trimEnd('/') + "/chat/completions"
                val payload = mapOf(
                    "model" to model,
                    "messages" to request.messages.map { message ->
                        mapOf(
                            "role" to normalizeOpenAiRole(message.role),
                            "content" to message.content
                        )
                    },
                    "temperature" to request.temperature,
                    "max_tokens" to request.maxTokens
                ).filterValues { it != null }

                val httpRequest = Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(gson.toJson(payload).toRequestBody(jsonMediaType))
                    .build()

                okHttpClient.newCall(httpRequest).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("NVIDIA NIM error ${response.code}: ${responseBody.take(240)}")
                        )
                    }

                    val parsed = gson.fromJson(responseBody, AiChatResponse::class.java)
                    val content = parsed.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        .orEmpty()
                        .trim()

                    if (content.isBlank()) {
                        Result.failure(IllegalStateException("NVIDIA NIM devolvio una respuesta vacia."))
                    } else {
                        Result.success(parsed)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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
                    )
                )

                val result = callGemini(request)
                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Error en Gemini")
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
                    Result.failure(Exception("La IA no devolvio recomendaciones."))
                } else {
                    Result.success(assistantMessage)
                }
            } catch (e: Exception) {
                Result.failure(e)
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
                    temperature = 0.2,
                    maxTokens = 220
                )

                val result = callGemini(request)
                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Error en Gemini")
                    )
                }

                val content = result.getOrNull()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    .orEmpty()

                Result.success(parseCategorySuggestion(content))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun analyzePostForModeration(post: Post): Result<ModerationAiAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AiChatRequest(
                    messages = listOf(
                        AiMessage(
                            role = "system",
                            content = "Eres un asistente de moderacion para PetHelp. Analiza publicaciones de mascotas con criterio responsable, claro y seguro. No apruebas ni rechazas por tu cuenta: ayudas al moderador humano."
                        ),
                        AiMessage(role = "user", content = buildModerationPrompt(post))
                    ),
                    temperature = 0.25,
                    maxTokens = 700
                )

                val result = callGemini(request)
                if (result.isFailure) {
                    return@withContext Result.failure(
                        result.exceptionOrNull() ?: Exception("Error en Gemini")
                    )
                }

                val content = result.getOrNull()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    .orEmpty()
                    .trim()

                if (content.isBlank()) {
                    Result.failure(Exception("La IA no devolvio analisis de moderacion."))
                } else {
                    Result.success(parseModerationAnalysis(content))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun normalizeMessage(message: AiMessage): AiMessage {
        if (!message.role.equals("system", ignoreCase = true)) {
            return message
        }
        return message.copy(
            role = "user",
            content = "INSTRUCCIONES DEL SISTEMA:\n${message.content}"
        )
    }

    private fun normalizeRole(role: String): String {
        return when (role.lowercase()) {
            "assistant", "model" -> "model"
            else -> "user"
        }
    }

    private fun normalizeOpenAiRole(role: String): String {
        return when (role.lowercase()) {
            "assistant", "model" -> "assistant"
            "system" -> "system"
            else -> "user"
        }
    }

    private fun buildQuizPrompt(answers: Map<String, String>): String {
        return """
            Basado en las siguientes preferencias del usuario sobre mascotas, proporciona recomendaciones claras, utiles y accionables:

            Preferencias:
            ${answers.entries.joinToString("\n") { (key, value) -> "- $key: $value" }}

            Responde en espanol con este formato:
            PERFIL_IDEAL=<1 frase sobre el perfil del adoptante>
            RECOMENDACIONES=<3 recomendaciones separadas por |. Cada una debe incluir tipo de mascota y razon>
            CUIDADOS=<3 compromisos importantes separados por |>
            SIGUIENTE_PASO=<1 accion concreta dentro de PetHelp>

            Evita texto extra fuera de ese formato.
        """.trimIndent()
    }

    private fun buildModerationPrompt(post: Post): String {
        return """
            Analiza esta publicacion pendiente de moderacion en PetHelp.

            Datos:
            - Titulo: ${post.title}
            - Descripcion: ${post.description}
            - Categoria: ${post.category}
            - Estado: ${post.status}
            - Autor: ${post.authorName.ifBlank { "Sin nombre" }} (${post.authorId})
            - Tipo de animal: ${post.animalType}
            - Raza: ${post.breed}
            - Edad: ${post.age}
            - Sexo: ${post.gender}
            - Tamano: ${post.size}
            - Vacunado: ${post.vaccinated}
            - Desparasitado: ${post.dewormed}
            - Esterilizado: ${post.sterilized}
            - Cuidados especiales: ${post.specialCares}
            - Comportamiento: ${post.behavior.joinToString(", ")}
            - Fotos: ${post.imageUrls.size}
            - Ciudad: ${post.city}
            - Barrio: ${post.neighborhood}
            - Ubicacion: ${post.locationName}
            - Razon de rechazo previa: ${post.rejectionReason.orEmpty()}

            Evalua claridad, completitud, coherencia, riesgos de seguridad, datos sensibles y si el moderador debe pedir correcciones.

            Responde exactamente con este formato:
            SUMMARY=<maximo 220 caracteres>
            RISK_LEVEL=<LOW|MEDIUM|HIGH>
            CONFIDENCE=<numero 0 a 100>
            RECOMMENDATION=<aprobar, revisar o rechazar con una razon breve>
            STRENGTHS=<fortalezas separadas por |>
            CONCERNS=<alertas separadas por |>
            MISSING_FIELDS=<campos faltantes o debiles separados por |>
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

    private fun parseModerationAnalysis(content: String): ModerationAiAnalysis {
        val summary = extractLine(content, "SUMMARY").ifBlank {
            content.lineSequence().firstOrNull()?.trim().orEmpty().take(220)
        }
        val riskLevel = when (extractLine(content, "RISK_LEVEL").uppercase()) {
            "HIGH" -> ModerationRiskLevel.HIGH
            "MEDIUM" -> ModerationRiskLevel.MEDIUM
            else -> ModerationRiskLevel.LOW
        }
        val confidence = extractLine(content, "CONFIDENCE")
            .filter { it.isDigit() }
            .toIntOrNull()
            ?.coerceIn(0, 100)
            ?: 70
        val recommendation = extractLine(content, "RECOMMENDATION").ifBlank {
            "Revisar manualmente antes de tomar una decision."
        }

        return ModerationAiAnalysis(
            summary = summary,
            riskLevel = riskLevel,
            confidence = confidence,
            recommendation = recommendation,
            strengths = extractList(content, "STRENGTHS"),
            concerns = extractList(content, "CONCERNS"),
            missingFields = extractList(content, "MISSING_FIELDS")
        )
    }

    private fun extractLine(content: String, key: String): String {
        return Regex("""^$key\s*=\s*(.*)$""", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
            .find(content)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()
    }

    private fun extractList(content: String, key: String): List<String> {
        return extractLine(content, key)
            .split("|")
            .map { it.trim().trim('-', '•') }
            .filter { it.isNotBlank() }
            .take(5)
    }
}
