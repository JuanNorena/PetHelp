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
import javax.inject.Inject

@HiltViewModel
class AdoptionRequestViewModel @Inject constructor(
    private val repository: PostRepository,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    var state by mutableStateOf(AdoptionRequestState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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

    private fun submitRequest(postId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        repository.requestAdoption(
            postId = postId,
            userId = userId,
            message = state.message,
            housingType = state.housingType,
            hasOutdoorSpace = state.hasOutdoorSpace,
            hasExperience = state.hasExperience,
            phone = state.phone,
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

    sealed class UiEvent {
        data class ShowSnackbar(val uiText: UiText?) : UiEvent()
        object Success : UiEvent()
    }
}

data class AdoptionRequestState(
    val isLoading: Boolean = false,
    val message: String = "",
    val housingType: String = "house",
    val hasOutdoorSpace: String = "yes",
    val hasExperience: String = "yes",
    val phone: String = "",
    val contactPreference: String = "pethelp"
)

sealed class AdoptionRequestEvent {
    data class OnMessageChange(val value: String) : AdoptionRequestEvent()
    data class OnHousingTypeChange(val value: String) : AdoptionRequestEvent()
    data class OnOutdoorSpaceChange(val value: String) : AdoptionRequestEvent()
    data class OnExperienceChange(val value: String) : AdoptionRequestEvent()
    data class OnPhoneChange(val value: String) : AdoptionRequestEvent()
    data class OnContactPreferenceChange(val value: String) : AdoptionRequestEvent()
    data class Submit(val postId: String) : AdoptionRequestEvent()
}
