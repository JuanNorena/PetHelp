package com.pethelp.app.features.post.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.common.UiText
import com.pethelp.app.features.post.domain.model.AdoptionRequest
import com.pethelp.app.features.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AdoptionRequestsViewModel @Inject constructor(
    private val repository: PostRepository,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    var state by mutableStateOf(AdoptionRequestsState())
        private set

    init {
        loadRequests()
    }

    fun loadRequests() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        repository.getAdoptionRequestsForUser(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> state = state.copy(isLoading = true, error = null)
                is Resource.Success -> state = state.copy(isLoading = false, requests = result.data ?: emptyList(), error = null)
                is Resource.Error -> state = state.copy(isLoading = false, error = result.uiText)
            }
        }.launchIn(viewModelScope)
    }

    fun acceptRequest(request: AdoptionRequest) {
        repository.acceptAdoptionRequest(request.id, request.postId).onEach { result ->
            when (result) {
                is Resource.Loading -> state = state.copy(isActionLoading = true, error = null)
                is Resource.Success -> {
                    state = state.copy(isActionLoading = false, error = null)
                    loadRequests() // Recargar lista
                }
                is Resource.Error -> state = state.copy(isActionLoading = false, error = result.uiText)
            }
        }.launchIn(viewModelScope)
    }

    fun rejectRequest(requestId: String) {
        repository.rejectAdoptionRequest(requestId).onEach { result ->
            when (result) {
                is Resource.Loading -> state = state.copy(isActionLoading = true, error = null)
                is Resource.Success -> {
                    state = state.copy(isActionLoading = false, error = null)
                    loadRequests()
                }
                is Resource.Error -> state = state.copy(isActionLoading = false, error = result.uiText)
            }
        }.launchIn(viewModelScope)
    }
}

data class AdoptionRequestsState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val requests: List<AdoptionRequest> = emptyList(),
    val error: UiText? = null
)
