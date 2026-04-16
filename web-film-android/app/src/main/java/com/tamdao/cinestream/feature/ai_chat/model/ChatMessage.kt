package com.tamdao.cinestream.feature.ai_chat.model

import com.tamdao.cinestream.core.util.UiText
import com.tamdao.cinestream.data.model.MovieDto
import java.util.UUID

/**
 * Domain model representing a single message in the AI Chat.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: UiText,
    val isFromUser: Boolean,
    val movies: List<MovieDto> = emptyList(), // Displayed horizontally if not empty
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
