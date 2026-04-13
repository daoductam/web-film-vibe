package com.tamdao.cinestream.core.network

import com.tamdao.cinestream.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface AuthApiService {

    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @POST("v1/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): ApiResponse<AuthResponse>

    @GET("v1/users/me")
    suspend fun getProfile(): ApiResponse<UserDto>

    @PUT("v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserDto>

    @PUT("v1/users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>

    @Multipart
    @POST("v1/users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiResponse<UserDto>

    // Sync & Data Endpoints
    @GET("v1/users/me/favorites")
    suspend fun getFavorites(): ApiResponse<List<FavoriteResponse>>

    @POST("v1/users/me/favorites")
    suspend fun addFavorite(@Body request: SyncFavoriteRequest): ApiResponse<FavoriteResponse>

    @DELETE("v1/users/me/favorites/{slug}")
    suspend fun removeFavorite(@Path("slug") slug: String): ApiResponse<Unit>

    @POST("v1/users/me/favorites/sync")
    suspend fun syncFavorites(@Body requests: List<SyncFavoriteRequest>): ApiResponse<List<FavoriteResponse>>

    @GET("v1/users/me/history")
    suspend fun getHistory(): ApiResponse<List<WatchHistoryResponse>>

    @POST("v1/users/me/history")
    suspend fun saveHistory(@Body request: WatchHistoryRequest): ApiResponse<WatchHistoryResponse>

    @POST("v1/users/me/history/sync")
    suspend fun syncHistory(@Body requests: List<WatchHistoryRequest>): ApiResponse<List<WatchHistoryResponse>>
}

// Additional sync DTOs for the API service if not already defined (adding here for clarity or moving to AuthDto)
data class SyncFavoriteRequest(
    val movieSlug: String,
    val title: String,
    val thumbUrl: String?,
    val quality: String?,
    val year: Int?
)

data class FavoriteResponse(
    val movieSlug: String,
    val title: String,
    val thumbUrl: String?,
    val quality: String?,
    val year: Int?,
    val createdAt: String
)

data class WatchHistoryRequest(
    val movieSlug: String,
    val title: String,
    val thumbUrl: String?,
    val lastEpisodeSlug: String?,
    val lastEpisodeName: String?,
    val progressMs: Long?,
    val durationMs: Long?
)

data class WatchHistoryResponse(
    val movieSlug: String,
    val title: String,
    val thumbUrl: String?,
    val lastEpisodeSlug: String?,
    val lastEpisodeName: String?,
    val progressMs: Long?,
    val durationMs: Long?,
    val updatedAt: String
)
