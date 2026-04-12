package com.tamdao.cinestream.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.session.AuthViewModel
import com.tamdao.cinestream.core.session.SessionManager
import com.tamdao.cinestream.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Usually ProfileViewModel would handle profile-specific actions
    // while SessionManager/AuthViewModel handles the global state.
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
