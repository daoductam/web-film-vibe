package com.tamdao.cinestream.feature.ai_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.feature.ai_chat.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val repository: AIChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "Xin chào! Tôi là AI CineGuru. Bạn muốn tìm phim gì hôm nay?",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(content = text.trim(), isFromUser = true)
        val loadingMessage = ChatMessage(content = "Đang suy nghĩ...", isFromUser = false, isLoading = true)

        _messages.update { current -> current + userMessage + loadingMessage }

        viewModelScope.launch {
            repository.chatWithAi(text.trim()).fold(
                onSuccess = { response ->
                    _messages.update { current ->
                        val updatedList = current.toMutableList()
                        // Replace the last loading message
                        val index = updatedList.indexOfLast { it.isLoading }
                        if (index != -1) {
                            updatedList[index] = ChatMessage(
                                content = response.aiMessage,
                                isFromUser = false,
                                movies = response.movies?.content ?: emptyList()
                            )
                        }
                        updatedList
                    }
                },
                onFailure = { error ->
                    _messages.update { current ->
                        val updatedList = current.toMutableList()
                        // Replace the last loading message
                        val index = updatedList.indexOfLast { it.isLoading }
                        if (index != -1) {
                            updatedList[index] = ChatMessage(
                                content = "Đã có lỗi xảy ra: ${error.message ?: "Mất kết nối"}",
                                isFromUser = false,
                                isError = true
                            )
                        }
                        updatedList
                    }
                }
            )
        }
    }
}
