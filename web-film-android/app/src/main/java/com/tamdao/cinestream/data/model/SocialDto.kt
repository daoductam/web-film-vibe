package com.tamdao.cinestream.data.model

import com.google.gson.annotations.SerializedName

data class CommentDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val content: String,
    val parentId: Long?,
    val likeCount: Long,
    @SerializedName("liked")
    val isLiked: Boolean,
    val movieSlug: String?,
    val episodeSlug: String?,
    val episodeName: String?,
    val replies: List<CommentDto>,
    val createdAt: String,
    val updatedAt: String?
)

data class RatingDto(
    val averageRating: Double,
    val totalRatings: Long,
    val userRating: Int?
)

data class CommentRequest(
    val movieSlug: String?,
    val episodeSlug: String,
    val content: String,
    val parentId: Long?
)

data class RatingRequest(
    val movieSlug: String,
    val score: Int
)
