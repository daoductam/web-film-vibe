package com.tamdao.cinestream.feature.ai_chat

import com.tamdao.cinestream.core.network.MovieApiService
import com.tamdao.cinestream.feature.ai_chat.model.AIChatRequest
import com.tamdao.cinestream.feature.ai_chat.model.AIChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AIChatRepository @Inject constructor(
    private val apiService: MovieApiService
) {
    suspend fun chatWithAi(message: String): Result<AIChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.chatWithAi(AIChatRequest(message))
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error occurred"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
