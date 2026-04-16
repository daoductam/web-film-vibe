package com.tamdao.cinestream.core.network

import com.tamdao.cinestream.data.model.*
import retrofit2.http.*

interface SocialApiService {

    // Comments
    @GET("v1/comments/movie/{movieSlug}")
    suspend fun getMovieComments(
        @Path("movieSlug") movieSlug: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<CommentDto>>

    @GET("v1/comments/movie/{movieSlug}/episode/{slug}")
    suspend fun getComments(
        @Path("movieSlug") movieSlug: String,
        @Path("slug") episodeSlug: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResult<CommentDto>>

    @POST("v1/comments")
    suspend fun addComment(
        @Body request: CommentRequest
    ): ApiResponse<CommentDto>

    @DELETE("v1/comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: Long
    ): ApiResponse<Unit>

    @POST("v1/comments/{id}/like")
    suspend fun toggleLike(
        @Path("id") commentId: Long
    ): ApiResponse<Boolean>

    // Ratings
    @GET("v1/ratings/{movieSlug}")
    suspend fun getMovieRating(
        @Path("movieSlug") movieSlug: String
    ): ApiResponse<RatingDto>

    @POST("v1/ratings")
    suspend fun submitRating(
        @Body request: RatingRequest
    ): ApiResponse<Unit>
}
