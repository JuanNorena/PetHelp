/**
 * Contrato del repositorio de Inteligencia Artificial.
 *
 * Define operaciones de chat conversacional, recomendación de mascotas,
 * sugerencia de categoría para publicaciones y análisis de moderación
 * de contenido con modelos de lenguaje (Gemini/NVIDIA).
 */
package com.pethelp.app.features.ai.domain.repository

import com.google.gson.annotations.SerializedName
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostCategory

/**
 * Representa un mensaje dentro de una conversación con el modelo de IA.
 *
 * @param role Rol del emisor: "user" para el usuario o "assistant" para la IA.
 * @param content Texto del mensaje.
 * @param reasoningDetails Detalles de razonamiento del modelo (opcional, usado por NVIDIA).
 */
data class AiMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    @SerializedName("reasoning_details")
    val reasoningDetails: List<ReasoningDetail>? = null
)

/**
 * Detalle de razonamiento devuelto por modelos que soportan chain-of-thought.
 *
 * @param type Tipo de razonamiento.
 * @param id Identificador del detalle.
 * @param format Formato del contenido.
 * @param index Índice dentro de la secuencia.
 * @param text Texto del razonamiento.
 * @param summary Resumen del razonamiento.
 * @param data Datos adicionales.
 * @param signature Firma del contenido para verificación.
 */
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

/**
 * Petición enviada al modelo de lenguaje (Gemini o NVIDIA).
 *
 * @param model Identificador del modelo a usar.
 * @param messages Historial de mensajes de la conversación.
 * @param reasoning Habilita o deshabilita el razonamiento del modelo.
 * @param temperature Controla la creatividad (0 = determinista, 1 = creativo).
 * @param maxTokens Límite máximo de tokens en la respuesta.
 */
data class AiChatRequest(
    val model: String = "gemini-2.5-flash-lite",
    val messages: List<AiMessage>,
    val reasoning: Map<String, Boolean> = mapOf("enabled" to true),
    val temperature: Double = 0.35,
    @SerializedName("max_tokens")
    val maxTokens: Int? = 900
)

/**
 * Respuesta completa del modelo de lenguaje.
 *
 * @param id Identificador único de la respuesta.
 * @param choices Lista de opciones generadas (típicamente una).
 * @param usage Estadísticas de uso de tokens.
 */
data class AiChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

/**
 * Opción individual dentro de la respuesta del modelo.
 *
 * @param index Índice de la opción.
 * @param message Mensaje generado por el asistente.
 * @param finishReason Razón por la que terminó la generación (ej. "stop").
 */
data class Choice(
    val index: Int? = null,
    val message: AiMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * Estadísticas de consumo de tokens en una llamada a la IA.
 *
 * @param promptTokens Tokens usados en el prompt.
 * @param completionTokens Tokens generados en la respuesta.
 * @param totalTokens Total de tokens consumidos.
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)

/**
 * Sugerencia de categoría generada por la IA para una publicación.
 *
 * @param category Categoría sugerida (adopción, perdido, etc.).
 * @param confidence Nivel de confianza de 0 a 100.
 * @param reason Explicación textual de por qué se sugiere esa categoría.
 */
data class AiCategorySuggestion(
    val category: PostCategory,
    val confidence: Int,
    val reason: String
)

/**
 * Niveles de riesgo para el análisis de moderación de publicaciones.
 */
enum class ModerationRiskLevel {
    /** Contenido seguro, cumple las políticas. */
    LOW,

    /** Contenido que requiere revisión manual. */
    MEDIUM,

    /** Contenido potencialmente problemático, rechazo recomendado. */
    HIGH
}

/**
 * Resultado del análisis de moderación asistido por IA.
 *
 * @param summary Resumen textual del análisis.
 * @param riskLevel Nivel de riesgo evaluado.
 * @param confidence Confianza de la evaluación (0-100).
 * @param recommendation Recomendación para el moderador.
 * @param strengths Aspectos positivos detectados en la publicación.
 * @param concerns Problemas o preocupaciones identificadas.
 * @param missingFields Campos obligatorios que faltan en la publicación.
 */
data class ModerationAiAnalysis(
    val summary: String,
    val riskLevel: ModerationRiskLevel,
    val confidence: Int,
    val recommendation: String,
    val strengths: List<String> = emptyList(),
    val concerns: List<String> = emptyList(),
    val missingFields: List<String> = emptyList()
)

/**
 * Contrato del repositorio de Inteligencia Artificial.
 *
 * Define las operaciones que la app puede realizar con modelos de lenguaje:
 * - Chat conversacional con historial.
 * - Recomendación de mascotas basada en un quiz.
 * - Sugerencia automática de categoría para nuevas publicaciones.
 * - Análisis de moderación de contenido.
 *
 * La implementación concreta ([AiChatRepositoryImpl]) usa Gemini como proveedor
 * principal y NVIDIA NIM como fallback directo desde Android.
 */
interface AiChatRepository {
    /**
     * Envía una petición de chat al modelo de lenguaje.
     *
     * @param request Petición con historial de mensajes y parámetros del modelo.
     * @return Resultado con la respuesta del modelo o un error.
     */
    suspend fun callGemini(request: AiChatRequest): Result<AiChatResponse>

    /**
     * Genera recomendaciones de mascotas basadas en las respuestas del quiz.
     *
     * @param answers Mapa de preguntas a respuestas del usuario.
     * @return Texto con las recomendaciones generadas por la IA.
     */
    suspend fun getRecommendedPetsBasedOnQuiz(answers: Map<String, String>): Result<String>

    /**
     * Sugiere la categoría más apropiada para una nueva publicación.
     *
     * @param title Título de la publicación.
     * @param description Descripción de la publicación.
     * @param animalType Tipo de animal (perro, gato, etc.).
     * @return Sugerencia con categoría, confianza y justificación.
     */
    suspend fun suggestPostCategory(
        title: String,
        description: String,
        animalType: String
    ): Result<AiCategorySuggestion>

    /**
     * Analiza una publicación para asistir en la moderación.
     *
     * @param post Publicación a evaluar.
     * @return Análisis con riesgo, recomendación y campos faltantes.
     */
    suspend fun analyzePostForModeration(post: Post): Result<ModerationAiAnalysis>
}
