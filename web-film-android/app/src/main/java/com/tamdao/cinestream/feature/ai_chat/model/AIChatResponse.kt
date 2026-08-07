package com.tamdao.cinestream.feature.ai_chat.model

import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.model.PageResult

/**
 * Response payload from the AI Chat API.
 */
data class AIChatResponse(
    val isMovieQuery: Boolean,
    val aiMessage: String,
    val movies: PageResult<MovieDto>?
)
