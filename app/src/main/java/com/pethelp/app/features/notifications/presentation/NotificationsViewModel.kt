package com.pethelp.app.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethelp.app.core.common.Resource
import com.pethelp.app.features.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        notificationRepository.observeNotifications().onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }
                is Resource.Success -> {
                    _uiState.value = NotificationsUiState(
                        isLoading = false,
                        notifications = resource.data ?: emptyList(),
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun markAsRead(notificationId: String) {
        notificationRepository.markAsRead(notificationId).launchIn(viewModelScope)
    }

    fun markAllAsRead() {
        notificationRepository.markAllAsRead().launchIn(viewModelScope)
    }
}
