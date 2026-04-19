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
 * Clase de utilidad para manejar strings que pueden ser literales o recursos.
 * Permite que los ViewModels permanezcan puros (sin depender de Context)
 * pero soporten localización.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    companion object {
        fun fromCategory(category: PostCategory): UiText {
            return when (category) {
                PostCategory.ADOPTION -> StringResource(R.string.category_adoption)
                PostCategory.LOST -> StringResource(R.string.category_lost)
                PostCategory.FOUND -> StringResource(R.string.category_found)
                PostCategory.TEMP_HOME -> StringResource(R.string.category_temp_home)
                PostCategory.VET_EVENT -> StringResource(R.string.category_vet_event)
            }
        }

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

        fun fromSize(size: AnimalSize): UiText {
            return when (size) {
                AnimalSize.SMALL -> StringResource(R.string.tag_small)
                AnimalSize.MEDIUM -> StringResource(R.string.tag_medium)
                AnimalSize.LARGE -> StringResource(R.string.tag_large)
            }
        }

        fun fromAge(age: AnimalAge): UiText {
            return when (age) {
                AnimalAge.PUPPY -> StringResource(R.string.post_age_puppy)
                AnimalAge.YOUNG -> StringResource(R.string.post_age_young)
                AnimalAge.ADULT -> StringResource(R.string.post_age_adult)
                AnimalAge.SENIOR -> StringResource(R.string.post_age_senior)
            }
        }

        fun fromGender(gender: AnimalGender): UiText {
            return when (gender) {
                AnimalGender.MALE -> StringResource(R.string.post_gender_male)
                AnimalGender.FEMALE -> StringResource(R.string.post_gender_female)
                AnimalGender.UNKNOWN -> StringResource(R.string.post_gender_unknown)
            }
        }

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

        fun fromUserLevel(level: UserLevel): UiText {
            return when (level) {
                UserLevel.FRIEND -> StringResource(R.string.user_level_friend)
                UserLevel.PROTECTOR -> StringResource(R.string.user_level_protector)
                UserLevel.GUARDIAN -> StringResource(R.string.user_level_guardian)
                UserLevel.HERO -> StringResource(R.string.user_level_hero)
            }
        }

        fun fromHousingType(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "house", "casa" -> StringResource(R.string.housing_house)
                "apartment", "apto", "apartamento", "departamento" -> StringResource(R.string.housing_apartment)
                else -> DynamicString(key)
            }
        }

        fun fromYesNo(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "yes", "si", "sí", "true" -> StringResource(R.string.common_yes)
                "no", "false" -> StringResource(R.string.common_no)
                else -> DynamicString(key)
            }
        }

        fun fromExperience(key: String): UiText {
            return when (key.trim().lowercase(Locale.ROOT)) {
                "yes", "si", "sí", "tengo experiencia", "sí tengo", "si tengo" -> StringResource(R.string.experience_yes)
                "no", "soy nuevo", "no tengo" -> StringResource(R.string.experience_no)
                else -> DynamicString(key)
            }
        }

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
