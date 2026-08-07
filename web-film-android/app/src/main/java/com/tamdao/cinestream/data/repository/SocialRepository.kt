package com.tamdao.cinestream.data.repository

import com.tamdao.cinestream.core.network.SocialApiService
import com.tamdao.cinestream.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val socialApiService: SocialApiService
) {

    suspend fun getMovieComments(movieSlug: String, page: Int = 0): ApiResponse<PageResult<CommentDto>> {
        return socialApiService.getMovieComments(movieSlug, page)
    }

    suspend fun getComments(movieSlug: String, episodeSlug: String, page: Int = 0): ApiResponse<PageResult<CommentDto>> {
        return socialApiService.getComments(movieSlug, episodeSlug, page)
    }

    suspend fun addComment(
        movieSlug: String?,
        episodeSlug: String,
        content: String,
        parentId: Long? = null
    ): ApiResponse<CommentDto> {
        val request = CommentRequest(movieSlug, episodeSlug, content, parentId)
        return socialApiService.addComment(request)
    }

    suspend fun deleteComment(commentId: Long): ApiResponse<Unit> {
        return socialApiService.deleteComment(commentId)
    }

    suspend fun toggleLike(commentId: Long): ApiResponse<Boolean> {
        return socialApiService.toggleLike(commentId)
    }

    suspend fun getMovieRating(movieSlug: String): ApiResponse<RatingDto> {
        return socialApiService.getMovieRating(movieSlug)
    }

    suspend fun submitRating(movieSlug: String, score: Int): ApiResponse<Unit> {
        val request = RatingRequest(movieSlug, score)
        return socialApiService.submitRating(request)
    }
}
