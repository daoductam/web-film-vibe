package com.tamdao.cinestream.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.NotificationDto
import com.tamdao.cinestream.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            try {
                val response = repository.getNotifications()
                if (response.success && response.data != null) {
                    val items = response.data.content
                    _uiState.value = if (items.isEmpty()) {
                        NotificationUiState.Empty
                    } else {
                        NotificationUiState.Success(items)
                    }
                } else {
                    _uiState.value = NotificationUiState.Error(response.message ?: "Đã xảy ra lỗi")
                }
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.localizedMessage ?: "Không thể tải thông báo")
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val response = repository.getUnreadCount()
                if (response.success && response.data != null) {
                    _unreadCount.value = response.data.unreadCount
                }
            } catch (_: Exception) {
                // Silently fail - badge count is non-critical
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = repository.getNotifications()
                if (response.success && response.data != null) {
                    val items = response.data.content
                    _uiState.value = if (items.isEmpty()) {
                        NotificationUiState.Empty
                    } else {
                        NotificationUiState.Success(items)
                    }
                }
                loadUnreadCount()
            } catch (_: Exception) {
                // Keep current state on refresh failure
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsRead(notificationId)
                // Update local state
                val currentState = _uiState.value
                if (currentState is NotificationUiState.Success) {
                    val updatedList = currentState.notifications.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                    _uiState.value = NotificationUiState.Success(updatedList)
                }
                loadUnreadCount()
            } catch (_: Exception) {
                // Silently fail
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllAsRead()
                // Update local state
                val currentState = _uiState.value
                if (currentState is NotificationUiState.Success) {
                    val updatedList = currentState.notifications.map { it.copy(isRead = true) }
                    _uiState.value = NotificationUiState.Success(updatedList)
                }
                _unreadCount.value = 0
            } catch (_: Exception) {
                // Silently fail
            }
        }
    }
}

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    object Empty : NotificationUiState()
    data class Success(val notifications: List<NotificationDto>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}
