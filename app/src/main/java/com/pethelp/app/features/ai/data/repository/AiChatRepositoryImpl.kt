package com.pethelp.app.features.ai.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.features.ai.domain.repository.AiCategorySuggestion
import com.pethelp.app.features.ai.domain.repository.AiChatRepository
import com.pethelp.app.features.ai.domain.repository.AiChatRequest
import com.pethelp.app.features.ai.domain.repository.AiChatResponse
import com.pethelp.app.features.ai.domain.repository.AiMessage
import com.pethelp.app.features.ai.domain.repository.Choice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AiChatRepositoryImpl @Inject constructor() : AiChatRepository {

    override suspend fun callGemini(request: AiChatRequest): Result<AiChatResponse> {
        return withContext(Dispatchers.IO) {
            if (request.messages.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Empty AI request"))
            }

            try {
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
}
