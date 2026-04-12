package com.tamdao.cinestream.feature.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.session.SessionManager
import com.tamdao.cinestream.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _avatarUrl = mutableStateOf<String?>(null)
    val avatarUrl: State<String?> = _avatarUrl

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _updateSuccess = MutableSharedFlow<String>()
    val updateSuccess = _updateSuccess.asSharedFlow()

    init {
        viewModelScope.launch {
            val user = sessionManager.userInfo.first()
            user?.let {
                _fullName.value = it.fullName ?: ""
                _avatarUrl.value = it.avatarUrl
            }
        }
    }

    fun onFullNameChange(value: String) {
        _fullName.value = value
    }

    fun updateProfile() {
        if (_fullName.value.isBlank()) {
            _error.value = "Họ và tên không được để trống"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = authRepository.updateProfile(_fullName.value)
            
            result.onSuccess {
                _updateSuccess.emit("Cập nhật thông tin thành công")
            }.onFailure { e ->
                _error.value = e.localizedMessage ?: "Cập nhật thất bại"
            }
            
            _isLoading.value = false
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val result = authRepository.uploadAvatar(body)
                
                result.onSuccess {
                    _avatarUrl.value = it.avatarUrl
                    _updateSuccess.emit("Cập nhật ảnh đại diện thành công")
                }.onFailure { e ->
                    _error.value = e.localizedMessage ?: "Upload ảnh thất bại"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xử lý file: ${e.localizedMessage}"
            }
            
            _isLoading.value = false
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}
