package com.tamdao.cinestream.core.network

import com.tamdao.cinestream.data.model.ApiResponse
import com.tamdao.cinestream.data.model.NotificationDto
import com.tamdao.cinestream.data.model.PageResult
import com.tamdao.cinestream.data.model.UnreadCountDto
import retrofit2.http.*

interface NotificationApiService {

    @GET("v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<NotificationDto>>

    @PATCH("v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long
    ): ApiResponse<Unit>

    @POST("v1/notifications/read-all")
    suspend fun markAllAsRead(): ApiResponse<Unit>

    @GET("v1/notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<UnreadCountDto>
}
