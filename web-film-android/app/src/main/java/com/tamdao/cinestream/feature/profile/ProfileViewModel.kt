package com.tamdao.cinestream.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.session.AuthViewModel
import com.tamdao.cinestream.core.session.SessionManager
import com.tamdao.cinestream.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.tamdao.cinestream.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncState = _syncState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun syncFavorites() {
        viewModelScope.launch {
            _syncState.value = SyncStatus.Loading
            movieRepository.refreshFavorites()
            _syncState.value = SyncStatus.Success("Đã đồng bộ phim yêu thích")
            resetSyncState()
        }
    }

    fun syncHistory() {
        viewModelScope.launch {
            _syncState.value = SyncStatus.Loading
            movieRepository.refreshWatchHistory()
            _syncState.value = SyncStatus.Success("Đã đồng bộ lịch sử xem")
            resetSyncState()
        }
    }

    private fun resetSyncState() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _syncState.value = SyncStatus.Idle
        }
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Loading : SyncStatus()
    data class Success(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
