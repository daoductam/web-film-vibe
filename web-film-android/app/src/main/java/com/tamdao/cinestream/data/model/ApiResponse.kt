package com.tamdao.cinestream.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val timestamp: String?,
    val pagination: PageInfo? = null
)

// Spring Boot Page<T> JSON structure:
data class PageResult<T>(
    val content: List<T> = emptyList(),
    // Fallbacks for older Spring configurations
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 0,
    val number: Int = 0,        // current page (0-indexed)
    
    // Spring Boot 3 default serialization wraps metadata in "page"
    val page: SpringPageMetadata? = null
)

data class SpringPageMetadata(
    val size: Int = 0,
    val number: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

data class PageInfo(
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalItems: Long = 0,
    val itemsPerPage: Int = 0
)
