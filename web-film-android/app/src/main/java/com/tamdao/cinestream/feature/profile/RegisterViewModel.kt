package com.tamdao.cinestream.feature.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.RegisterRequest
import com.tamdao.cinestream.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _registerSuccess = MutableSharedFlow<Unit>()
    val registerSuccess = _registerSuccess.asSharedFlow()

    fun onUsernameChange(value: String) { _username.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value }

    fun register() {
        if (_username.value.isBlank() || _email.value.isBlank() || 
            _fullName.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        
        if (_password.value != _confirmPassword.value) {
            _error.value = "Mật khẩu xác nhận không khớp"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = authRepository.register(
                RegisterRequest(
                    username = _username.value,
                    email = _email.value,
                    fullName = _fullName.value,
                    password = _password.value
                )
            )

            result.onSuccess {
                _registerSuccess.emit(Unit)
            }.onFailure { e ->
                _error.value = e.localizedMessage ?: "Đăng ký thất bại"
            }
            
            _isLoading.value = false
        }
    }
}
