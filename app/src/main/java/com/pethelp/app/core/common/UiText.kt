package com.pethelp.app.core.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pethelp.app.core.domain.model.AnimalAge
import com.pethelp.app.core.domain.model.AnimalGender
import com.pethelp.app.core.domain.model.AnimalSize
import com.pethelp.app.core.domain.model.PostCategory
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.domain.model.PetBehavior
import com.pethelp.app.core.domain.model.UserLevel
import com.pethelp.app.R
import java.util.Locale

/**
 * Representa un texto de la UI que puede provenir de dos fuentes:
 * 1. Texto dinámico en tiempo de ejecución (`DynamicString`).
 * 2. Texto localizado desde recursos de Android (`StringResource`).
 *
 * Esta clase permite que los ViewModels trabajen sin depender de `Context`,
 * pero al mismo tiempo soporte localización y formateo de strings en la UI.
 */
sealed class UiText {

    /**
     * Texto literal generado en tiempo de ejecución.
     *
     * Ejemplo: nombre de usuario, mensaje recibido desde una API, valores libres.
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Texto basado en un recurso de Android.
     *
     * @param resId identificador de string en `R.string`.
     * @param args argumentos opcionales para formato.
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Convierte el texto en un `String` desde Compose.
     *
     * Esta función se usa cuando el componente Compose necesita mostrar el
     * texto en pantalla y puede acceder a los recursos.
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    /**
     * Convierte el texto en un `String` usando un `Context` clásico.
     *
     * Esta función es útil cuando no se está dentro de Compose, por ejemplo en
     * clases que reciben un `Context` para resolver recursos.
     */
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    companion object {
        /**
         * Convierte una categoría de publicación a su texto localizable.
         */
        fun fromCategory(category: PostCategory): UiText {
            return when (category) {
                PostCategory.ADOPTION -> StringResource(R.string.category_adoption)
                PostCategory.LOST -> StringResource(R.string.category_lost)
                PostCategory.FOUND -> StringResource(R.string.category_found)
                PostCategory.TEMP_HOME -> StringResource(R.string.category_temp_home)
                PostCategory.VET_EVENT -> StringResource(R.string.category_vet_event)
            }
        }

        /**
         * Convierte un estado de publicación a su texto localizable.
         */
        fun fromStatus(status: PostStatus): UiText {
            return when (status) {
                PostStatus.ACTIVE -> StringResource(R.string.status_active_caps)
                PostStatus.PAUSED -> StringResource(R.string.status_paused_caps)
                PostStatus.ADOPTED -> StringResource(R.string.status_adopted_caps)
                PostStatus.PENDING -> StringResource(R.string.status_pending_caps)
                PostStatus.VERIFIED -> StringResource(R.string.status_verified)
                PostStatus.REJECTED -> StringResource(R.string.status_rejected_caps)
                PostStatus.RESOLVED -> StringResource(R.string.status_resolved_caps)
            }
        }

        /**
         * Convierte el tamaño de un animal en un texto localizable.
         */
        fun fromSize(size: AnimalSize): UiText {
            return when (size) {
                AnimalSize.SMALL -> StringResource(R.string.tag_small)
                AnimalSize.MEDIUM -> StringResource(R.string.tag_medium)
                AnimalSize.LARGE -> StringResource(R.string.tag_large)
            }
        }

        /**
         * Convierte la edad del animal en un texto localizable.
         */
        fun fromAge(age: AnimalAge): UiText {
            return when (age) {
                AnimalAge.PUPPY -> StringResource(R.string.post_age_puppy)
                AnimalAge.YOUNG -> StringResource(R.string.post_age_young)
                AnimalAge.ADULT -> StringResource(R.string.post_age_adult)
                AnimalAge.SENIOR -> StringResource(R.string.post_age_senior)
            }
        }

        /**
         * Convierte el género del animal en un texto localizable.
         */
        fun fromGender(gender: AnimalGender): UiText {
            return when (gender) {
                AnimalGender.MALE -> StringResource(R.string.post_gender_male)
                AnimalGender.FEMALE -> StringResource(R.string.post_gender_female)
                AnimalGender.UNKNOWN -> StringResource(R.string.post_gender_unknown)
            }
        }

        /**
         * Convierte el comportamiento del animal en un texto localizable.
         */
        fun fromBehavior(behavior: PetBehavior): UiText {
            return when (behavior) {
                PetBehavior.PLAYFUL -> StringResource(R.string.post_behavior_playful)
                PetBehavior.CALM -> StringResource(R.string.post_behavior_calm)
                PetBehavior.PROTECTIVE -> StringResource(R.string.post_behavior_protective)
                PetBehavior.SHY -> StringResource(R.string.post_behavior_shy)
                PetBehavior.SOCIABLE -> StringResource(R.string.post_behavior_sociable)
                PetBehavior.INDEPENDENT -> StringResource(R.string.post_behavior_independent)
                PetBehavior.AFFECTIONATE -> StringResource(R.string.post_behavior_affectionate)
                PetBehavior.ACTIVE -> StringResource(R.string.post_behavior_active)
            }
        }

        /**
         * Convierte el nivel de usuario en un texto localizable.
         */
        fun fromUserLevel(level: UserLevel): UiText {
            return when (level) {
                UserLevel.FRIEND -> StringResource(R.string.user_level_friend)
                UserLevel.PROTECTOR -> StringResource(R.string.user_level_protector)
                UserLevel.GUARDIAN -> StringResource(R.string.user_level_guardian)
                UserLevel.HERO -> StringResource(R.string.user_level_hero)
            }
        }

        /**
         * Convierte una cadena libre representando el tipo de vivienda en texto.
         * Si la clave no coincide con ningún valor conocido, devuelve el texto original.
         */
        fun fromHousingType(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "house", "casa" -> StringResource(R.string.housing_house)
                "apartment", "apto", "apartamento", "departamento" -> StringResource(R.string.housing_apartment)
                else -> DynamicString(key)
            }
        }

        /**
         * Convierte una respuesta de sí/no en un texto localizable.
         */
        fun fromYesNo(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "yes", "si", "sí", "true" -> StringResource(R.string.common_yes)
                "no", "false" -> StringResource(R.string.common_no)
                else -> DynamicString(key)
            }
        }

        /**
         * Convierte un texto libre de experiencia en una etiqueta localizable.
         */
        fun fromExperience(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "yes", "si", "sí", "tengo experiencia", "sí tengo", "si tengo" -> StringResource(R.string.experience_yes)
                "no", "soy nuevo", "no tengo" -> StringResource(R.string.experience_no)
                else -> DynamicString(key)
            }
        }

        /**
         * Convierte una preferencia de contacto en un texto localizable.
         */
        fun fromContactPreference(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "pethelp", "chat", "chat en pethelp" -> StringResource(R.string.contact_pethelp)
                "whatsapp" -> StringResource(R.string.contact_whatsapp)
                "call", "phone", "telefono", "teléfono", "llamada telefónica" -> StringResource(R.string.contact_phone_call)
                else -> DynamicString(key)
            }
        }
    }
}
