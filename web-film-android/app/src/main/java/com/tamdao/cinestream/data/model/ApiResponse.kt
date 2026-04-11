package com.tamdao.cinestream.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val timestamp: String?,
    val pagination: PageInfo? = null
)

// Vì Backend trả về Page<T> trong trường 'data'
data class PageResult<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int
)

data class PageInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long,
    val itemsPerPage: Int
)
