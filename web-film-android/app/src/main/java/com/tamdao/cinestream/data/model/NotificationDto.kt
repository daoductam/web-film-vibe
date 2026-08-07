package com.tamdao.cinestream.data.model

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: Long,
    val title: String,
    val content: String,
    val type: String,
    val movieSlug: String,
    val thumbUrl: String?,
    @SerializedName("read")
    val isRead: Boolean,
    val createdAt: String
)

data class UnreadCountDto(
    val unreadCount: Long
)
