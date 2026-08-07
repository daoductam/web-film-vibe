package com.tamdao.cinestream.core.network

import com.tamdao.cinestream.data.model.*
import retrofit2.http.*

interface WatchPartyApiService {

    @POST("v1/watch-rooms")
    suspend fun createRoom(
        @Body request: CreateWatchRoomRequest
    ): ApiResponse<WatchRoomDto>

    @GET("v1/watch-rooms/public")
    suspend fun getPublicRooms(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResult<WatchRoomDto>>

    @GET("v1/watch-rooms/{id}")
    suspend fun getRoomById(
        @Path("id") id: Long
    ): ApiResponse<WatchRoomDto>

    @GET("v1/watch-rooms/code/{code}")
    suspend fun getRoomByCode(
        @Path("code") code: String
    ): ApiResponse<WatchRoomDto>

    @POST("v1/watch-rooms/{id}/join")
    suspend fun joinRoom(
        @Path("id") id: Long
    ): ApiResponse<WatchRoomMemberDto>

    @POST("v1/watch-rooms/{id}/leave")
    suspend fun leaveRoom(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    @PUT("v1/watch-rooms/{id}/end")
    suspend fun endRoom(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    @GET("v1/watch-rooms/{id}/state")
    suspend fun getVideoState(
        @Path("id") id: Long
    ): ApiResponse<VideoStateDto>

    @PUT("v1/watch-rooms/{id}/members/{userId}/role")
    suspend fun updateMemberRole(
        @Path("id") id: Long,
        @Path("userId") userId: Long,
        @Query("role") role: String
    ): ApiResponse<Unit>

    @GET("v1/watch-rooms/{id}/messages")
    suspend fun getMessages(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResult<ChatMessageDto>>

    @GET("v1/watch-rooms/history")
    suspend fun getWatchPartyHistory(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResult<WatchPartyHistoryDto>>
}
