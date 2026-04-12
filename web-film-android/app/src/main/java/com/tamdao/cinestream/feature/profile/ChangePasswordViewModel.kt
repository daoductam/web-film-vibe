package com.tamdao.cinestream.feature.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.ChangePasswordRequest
import com.tamdao.cinestream.core.network.AuthApiService
import com.tamdao.cinestream.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authApiService: com.tamdao.cinestream.core.network.AuthApiService
) : ViewModel() {

    private val _currentPassword = mutableStateOf("")
    val currentPassword: State<String> = _currentPassword

    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _success = MutableSharedFlow<String>()
    val success = _success.asSharedFlow()

    fun onCurrentPasswordChange(v: String) { _currentPassword.value = v }
    fun onNewPasswordChange(v: String) { _newPassword.value = v }
    fun onConfirmPasswordChange(v: String) { _confirmPassword.value = v }

    fun changePassword() {
        if (_currentPassword.value.isBlank() || _newPassword.value.isBlank()) {
            _error.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        if (_newPassword.value != _confirmPassword.value) {
            _error.value = "Mật khẩu xác nhận không khớp"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = authApiService.changePassword(
                    com.tamdao.cinestream.data.model.ChangePasswordRequest(
                        currentPassword = _currentPassword.value,
                        newPassword = _newPassword.value,
                        confirmPassword = _confirmPassword.value
                    )
                )
                
                if (response.success) {
                    _success.emit("Đổi mật khẩu thành công")
                    _currentPassword.value = ""
                    _newPassword.value = ""
                    _confirmPassword.value = ""
                } else {
                    _error.value = response.message ?: "Đổi mật khẩu thất bại"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Lỗi hệ thống"
            }
            
            _isLoading.value = false
        }
    }
}
