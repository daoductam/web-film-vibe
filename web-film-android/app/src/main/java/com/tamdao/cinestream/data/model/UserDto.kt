package com.tamdao.cinestream.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val avatarUrl: String?,
    val role: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpdateProfileRequest(
    val fullName: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)
