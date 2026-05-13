/**
 * ViewModel del formulario de solicitud de adopción.
 *
 * Administra los campos del formulario, valida la entrada del usuario
 * y ejecuta el envío de la solicitud de adopción a Firestore.
 */
package com.pethelp.app.features.post.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del formulario de solicitud de adopcion desde el detalle de publicacion.
 *
 * Administra los campos del formulario, ejecuta el envio de la solicitud y emite eventos
 * de una sola vez para exito o mensajes de error en la UI.
 */
@HiltViewModel
class AdoptionRequestViewModel @Inject constructor(
    private val repository: PostRepository,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    /** Estado editable del formulario de solicitud. */
    var state by mutableStateOf(AdoptionRequestState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()

    /** Flujo de eventos one-shot para snackbar y navegacion de exito. */
    val eventFlow = _eventFlow.asSharedFlow()

    /** Procesa interacciones de la UI sobre el formulario. */
    fun onEvent(event: AdoptionRequestEvent) {
        when (event) {
            is AdoptionRequestEvent.OnMessageChange -> state = state.copy(message = event.value)
            is AdoptionRequestEvent.OnHousingTypeChange -> state = state.copy(housingType = event.value)
            is AdoptionRequestEvent.OnOutdoorSpaceChange -> state = state.copy(hasOutdoorSpace = event.value)
            is AdoptionRequestEvent.OnExperienceChange -> state = state.copy(hasExperience = event.value)
            is AdoptionRequestEvent.OnPhoneChange -> state = state.copy(phone = event.value)
            is AdoptionRequestEvent.OnContactPreferenceChange -> state = state.copy(contactPreference = event.value)
            is AdoptionRequestEvent.Submit -> submitRequest(event.postId)
        }
    }

    /** Envia la solicitud al repositorio para la publicacion indicada. */
    private fun submitRequest(postId: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Inicia sesión para solicitar adopción.")))
            }
            return
        }

        val normalizedMessage = state.message.trim()
        val normalizedPhone = state.phone.trim()
        if (normalizedMessage.length < 20) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Cuéntale al publicador por qué quieres adoptar. Mínimo 20 caracteres.")))
            }
            return
        }
        if (normalizedPhone.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Ingresa un teléfono de contacto.")))
            }
            return
        }

        repository.requestAdoption(
            postId = postId,
            userId = userId,
            message = normalizedMessage,
            housingType = state.housingType,
            hasOutdoorSpace = state.hasOutdoorSpace,
            hasExperience = state.hasExperience,
            phone = normalizedPhone,
            contactPreference = state.contactPreference
        ).onEach { result ->
            when (result) {
                is Resource.Loading -> state = state.copy(isLoading = true)
                is Resource.Success -> {
                    state = state.copy(isLoading = false)
                    _eventFlow.emit(UiEvent.Success)
                }
                is Resource.Error -> {
                    state = state.copy(isLoading = false)
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText))
                }
            }
        }.launchIn(viewModelScope)
    }

    /** Eventos de salida para efectos de interfaz de una sola ejecucion. */
    sealed class UiEvent {
        data class ShowSnackbar(val uiText: UiText?) : UiEvent()
        object Success : UiEvent()
    }
}

/**
 * Estado del formulario de solicitud de adopcion.
 */
data class AdoptionRequestState(
    val isLoading: Boolean = false,
    val message: String = "",
    val housingType: String = "house",
    val hasOutdoorSpace: String = "yes",
    val hasExperience: String = "yes",
    val phone: String = "",
    val contactPreference: String = "pethelp"
)

/**
 * Eventos de entrada originados por la interfaz del formulario.
 */
sealed class AdoptionRequestEvent {
    data class OnMessageChange(val value: String) : AdoptionRequestEvent()
    data class OnHousingTypeChange(val value: String) : AdoptionRequestEvent()
    data class OnOutdoorSpaceChange(val value: String) : AdoptionRequestEvent()
    data class OnExperienceChange(val value: String) : AdoptionRequestEvent()
    data class OnPhoneChange(val value: String) : AdoptionRequestEvent()
    data class OnContactPreferenceChange(val value: String) : AdoptionRequestEvent()
    data class Submit(val postId: String) : AdoptionRequestEvent()
}
