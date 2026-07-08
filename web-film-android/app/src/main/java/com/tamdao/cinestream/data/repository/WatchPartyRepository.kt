package com.tamdao.cinestream.data.repository

import com.tamdao.cinestream.data.model.ApiResponse
import com.tamdao.cinestream.data.model.PageResult
import com.tamdao.cinestream.core.network.WatchPartyApiService
import com.tamdao.cinestream.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchPartyRepository @Inject constructor(
    private val apiService: WatchPartyApiService
) {

    suspend fun createRoom(request: CreateWatchRoomRequest): Result<WatchRoomDto> {
        return safeApiCall { apiService.createRoom(request) }
    }

    suspend fun getPublicRooms(page: Int, size: Int): Result<PageResult<WatchRoomDto>> {
        return safeApiCall { apiService.getPublicRooms(page, size) }
    }

    suspend fun getRoomById(id: Long): Result<WatchRoomDto> {
        return safeApiCall { apiService.getRoomById(id) }
    }

    suspend fun getRoomByCode(code: String): Result<WatchRoomDto> {
        return safeApiCall { apiService.getRoomByCode(code) }
    }

    suspend fun joinRoom(id: Long): Result<WatchRoomMemberDto> {
        return safeApiCall { apiService.joinRoom(id) }
    }

    suspend fun leaveRoom(id: Long): Result<Unit> {
        return safeApiCall { apiService.leaveRoom(id) }
    }

    suspend fun endRoom(id: Long): Result<Unit> {
        return safeApiCall { apiService.endRoom(id) }
    }

    suspend fun getVideoState(id: Long): Result<VideoStateDto> {
        return safeApiCall { apiService.getVideoState(id) }
    }

    suspend fun updateMemberRole(id: Long, userId: Long, role: String): Result<Unit> {
        return safeApiCall { apiService.updateMemberRole(id, userId, role) }
    }

    suspend fun getMessages(id: Long, page: Int, size: Int): Result<PageResult<ChatMessageDto>> {
        return safeApiCall { apiService.getMessages(id, page, size) }
    }

    suspend fun getWatchPartyHistory(page: Int, size: Int): Result<PageResult<WatchPartyHistoryDto>> {
        return safeApiCall { apiService.getWatchPartyHistory(page, size) }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val response = call()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else if (response.success) {
                @Suppress("UNCHECKED_CAST")
                Result.success(null as T)
            } else {
                Result.failure(Exception(response.message ?: "Yêu cầu xử lý thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
