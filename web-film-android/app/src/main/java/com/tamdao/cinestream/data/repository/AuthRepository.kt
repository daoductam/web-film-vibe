package com.tamdao.cinestream.data.repository

import com.tamdao.cinestream.core.network.AuthApiService
import com.tamdao.cinestream.core.session.SessionManager
import com.tamdao.cinestream.core.session.SessionUser
import com.tamdao.cinestream.data.model.*
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val movieDao: com.tamdao.cinestream.core.database.MovieDao
) {

    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.success && response.data != null) {
                val authData = response.data
                sessionManager.saveSession(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    user = authData.user.toSessionUser()
                )
                // Sync data after login
                syncDataAfterLogin()
                Result.success(authData)
            } else {
                Result.failure(Exception(response.message ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.success && response.data != null) {
                val authData = response.data
                sessionManager.saveSession(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    user = authData.user.toSessionUser()
                )
                // Sync data after register
                syncDataAfterLogin()
                Result.success(authData)
            } else {
                Result.failure(Exception(response.message ?: "Đăng ký thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncDataAfterLogin() {
        try {
            // 1. Sync Favorites
            val localFavs = movieDao.getAllFavorites().first()
            if (localFavs.isNotEmpty()) {
                val requests = localFavs.map {
                    com.tamdao.cinestream.core.network.SyncFavoriteRequest(
                        movieSlug = it.slug,
                        title = it.title,
                        thumbUrl = it.thumbUrl,
                        quality = it.quality,
                        year = it.year
                    )
                }
                apiService.syncFavorites(requests)
            }

            // 2. Sync History
            val localHistory = movieDao.getWatchHistory().first()
            if (localHistory.isNotEmpty()) {
                val requests = localHistory.map {
                    com.tamdao.cinestream.core.network.WatchHistoryRequest(
                        movieSlug = it.slug,
                        title = it.title,
                        thumbUrl = it.thumbUrl,
                        lastEpisodeSlug = it.lastEpisodeSlug,
                        lastEpisodeName = it.lastEpisodeName,
                        progressMs = it.progressMs,
                        durationMs = it.durationMs
                    )
                }
                apiService.syncHistory(requests)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Initial sync failed: ${e.localizedMessage}")
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun getProfile(): Result<UserDto> {
        return try {
            val response = apiService.getProfile()
            if (response.success && response.data != null) {
                sessionManager.updateUserInfo(response.data.toSessionUser())
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể lấy thông tin cá nhân"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(fullName: String): Result<UserDto> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(fullName))
            if (response.success && response.data != null) {
                sessionManager.updateUserInfo(response.data.toSessionUser())
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Cập nhật thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(file: MultipartBody.Part): Result<UserDto> {
        return try {
            val response = apiService.uploadAvatar(file)
            if (response.success && response.data != null) {
                sessionManager.updateUserInfo(response.data.toSessionUser())
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Upload ảnh thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserDto.toSessionUser() = SessionUser(
        id = id,
        username = username,
        email = email,
        fullName = fullName,
        avatarUrl = avatarUrl,
        role = role
    )
}
