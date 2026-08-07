package com.tamdao.cinestream.data.repository

import com.tamdao.cinestream.core.network.NotificationApiService
import com.tamdao.cinestream.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {

    suspend fun getNotifications(page: Int = 0, size: Int = 20): ApiResponse<PageResult<NotificationDto>> {
        return notificationApiService.getNotifications(page, size)
    }

    suspend fun markAsRead(notificationId: Long): ApiResponse<Unit> {
        return notificationApiService.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(): ApiResponse<Unit> {
        return notificationApiService.markAllAsRead()
    }

    suspend fun getUnreadCount(): ApiResponse<UnreadCountDto> {
        return notificationApiService.getUnreadCount()
    }
}
