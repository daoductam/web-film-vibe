package com.tamdao.cinestream.feature.watchparty

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.util.ErrorMapper
import com.tamdao.cinestream.core.websocket.StompManager
import com.tamdao.cinestream.data.model.*
import com.tamdao.cinestream.data.repository.WatchPartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tamdao.cinestream.core.session.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

import com.tamdao.cinestream.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class WatchPartyViewModel @Inject constructor(
    private val repository: WatchPartyRepository,
    private val movieRepository: MovieRepository,
    private val stompManager: StompManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun getCurrentUsername(): String {
        return runBlocking {
            sessionManager.userInfo.firstOrNull()?.username ?: ""
        }
    }


    private val tag = "WatchPartyViewModel"

    // Lobby UI state
    private val _lobbyState = MutableStateFlow<WatchPartyLobbyUiState>(WatchPartyLobbyUiState.Loading)
    val lobbyState: StateFlow<WatchPartyLobbyUiState> = _lobbyState.asStateFlow()

    // Room UI state
    private val _roomState = MutableStateFlow<WatchPartyRoomUiState>(WatchPartyRoomUiState.Loading)
    val roomState: StateFlow<WatchPartyRoomUiState> = _roomState.asStateFlow()

    // Movie Picker UI state
    private val _moviePickerState = MutableStateFlow<MoviePickerUiState>(MoviePickerUiState.Loading)
    val moviePickerState: StateFlow<MoviePickerUiState> = _moviePickerState.asStateFlow()

    private var searchJob: Job? = null

    // STOMP Connection State proxy
    val wsConnectionState = stompManager.connectionState

    // Live messages list inside room
    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages.asStateFlow()

    // Live reactions received
    private val _newReaction = MutableStateFlow<StompReaction?>(null)
    val newReaction: StateFlow<StompReaction?> = _newReaction.asStateFlow()

    // Live sync actions received
    private val _syncCommand = MutableStateFlow<StompSyncCommand?>(null)
    val syncCommand: StateFlow<StompSyncCommand?> = _syncCommand.asStateFlow()

    // Presence join/leave events
    private val _presenceEvent = MutableStateFlow<StompPresenceEvent?>(null)
    val presenceEvent: StateFlow<StompPresenceEvent?> = _presenceEvent.asStateFlow()

    // Active Room Detail
    private var activeRoomId: Long? = null

    data class StompReaction(val userId: Long, val username: String, val emoji: String, val timestamp: String)
    data class StompSyncCommand(val action: String, val timestamp: Double, val issuedBy: UserSummaryDto?, val serverTime: String?)
    data class StompPresenceEvent(val type: String, val user: UserSummaryDto, val memberCount: Int, val timestamp: String)

    fun loadPopularMoviesForPicker() {
        viewModelScope.launch {
            _moviePickerState.value = MoviePickerUiState.Loading
            try {
                // Tận dụng luồng Flow getPopularMovies hoặc filterMovies từ MovieRepository.
                // Ở đây ta gọi getMoviesByFilter với type = "popular" hoặc logic filter tương đương.
                val response = movieRepository.getMoviesByFilter(type = "popular", page = 0)
                if (response.success && response.data != null) {
                    _moviePickerState.value = MoviePickerUiState.Success(response.data.content)
                } else {
                    _moviePickerState.value = MoviePickerUiState.Error(response.message ?: "Lỗi tải phim phổ biến")
                }
            } catch (e: Exception) {
                _moviePickerState.value = MoviePickerUiState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun searchMoviesForPicker(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            loadPopularMoviesForPicker()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _moviePickerState.value = MoviePickerUiState.Loading
            try {
                val response = movieRepository.getMoviesByFilter(query = query, page = 0)
                if (response.success && response.data != null) {
                    _moviePickerState.value = MoviePickerUiState.Success(response.data.content)
                } else {
                    _moviePickerState.value = MoviePickerUiState.Error(response.message ?: "Không tìm thấy phim")
                }
            } catch (e: Exception) {
                _moviePickerState.value = MoviePickerUiState.Error(e.localizedMessage ?: "Lỗi tìm kiếm phim")
            }
        }
    }

    fun loadPublicRooms() {
        viewModelScope.launch {
            _lobbyState.value = WatchPartyLobbyUiState.Loading
            repository.getPublicRooms(0, 50)
                .onSuccess { page ->
                    _lobbyState.value = WatchPartyLobbyUiState.Success(page.content)
                }
                .onFailure { error ->
                    _lobbyState.value = WatchPartyLobbyUiState.Error(ErrorMapper.mapToString(error))
                }
        }
    }

    fun createRoom(request: CreateWatchRoomRequest, onSuccess: (WatchRoomDto) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            repository.createRoom(request)
                .onSuccess { room ->
                    onSuccess(room)
                }
                .onFailure { error ->
                    Log.e(tag, "Room creation failed: ${error.message}")
                    onFailure(ErrorMapper.mapToString(error))
                }
        }
    }

    fun joinRoomByCode(code: String, onSuccess: (WatchRoomDto) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            repository.getRoomByCode(code)
                .onSuccess { room ->
                    repository.joinRoom(room.id)
                        .onSuccess {
                            onSuccess(room)
                        }
                        .onFailure { err ->
                            onFailure(ErrorMapper.mapToString(err))
                        }
                }
                .onFailure { error ->
                    onFailure("Không tìm thấy phòng phù hợp với mã: $code")
                }
        }
    }

    fun enterRoom(roomId: Long) {
        activeRoomId = roomId
        _roomState.value = WatchPartyRoomUiState.Loading
        _messages.value = emptyList()

        viewModelScope.launch {
            // 1. Fetch room details
            val roomResult = repository.getRoomById(roomId)
            if (roomResult.isFailure) {
                _roomState.value = WatchPartyRoomUiState.Error("Không thể tải thông tin phòng")
                return@launch
            }
            val room = roomResult.getOrThrow()

            // Fetch actual episode streaming URL (linkM3u8)
            var videoUrl = ""
            try {
                val movieResponse = movieRepository.getMovieDetail(room.movie.slug)
                if (movieResponse.success && movieResponse.data != null) {
                    val allEpisodes = movieResponse.data.servers.flatMap { it.episodes }
                    val episodeSlug = room.episode?.slug
                    // Check matching episode or fallback to the first episode
                    val matchingEpisode = if (episodeSlug != null) {
                        allEpisodes.find { it.slug == episodeSlug }
                            ?: allEpisodes.find { it.slug == "${room.movie.slug}_$episodeSlug" }
                    } else {
                        null
                    } ?: allEpisodes.firstOrNull()
                    videoUrl = matchingEpisode?.linkM3u8 ?: ""
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch stream link for watch party: ${e.message}")
            }

            // 2. Fetch current sync state
            val stateResult = repository.getVideoState(roomId)
            val videoState = stateResult.getOrDefault(
                VideoStateDto(0.0, false, 1.0, LocalDateTime.now().toString())
            )

            // 3. Fetch initial chat history
            repository.getMessages(roomId, 0, 50)
                .onSuccess { page ->
                    _messages.value = page.content.reversed() // Oldest first for UI scroll
                }

            _roomState.value = WatchPartyRoomUiState.Success(
                room = room,
                messages = _messages.value,
                videoState = videoState,
                videoUrl = videoUrl
            )

            // 4. Connect to WebSockets and subscribe
            connectWebSocket(roomId)
        }
    }

    private fun connectWebSocket(roomId: Long) {
        // Build direct raw STOMP address based on BASE_URL configuration
        val baseUrl = com.tamdao.cinestream.core.util.Constants.BASE_URL
        val wsUrl = if (baseUrl.endsWith("/")) baseUrl + "ws" else baseUrl + "/ws"
        stompManager.connect(wsUrl)

        viewModelScope.launch {
            stompManager.connectionState.collectLatest { state ->
                if (state is StompManager.ConnectionState.Connected) {
                    subscribeToTopics(roomId)
                }
            }
        }
    }

    private fun subscribeToTopics(roomId: Long) {
        viewModelScope.launch {
            // Subscribe to Chat
            stompManager.subscribe("/topic/room.$roomId.chat", ChatMessageDto::class.java)
                .collectLatest { msg ->
                    _messages.value = _messages.value + msg
                }
        }

        viewModelScope.launch {
            // Subscribe to sync commands
            stompManager.subscribe("/topic/room.$roomId.sync", StompSyncCommand::class.java)
                .collectLatest { cmd ->
                    _syncCommand.value = cmd
                }
        }

        viewModelScope.launch {
            // Subscribe to Reactions
            stompManager.subscribe("/topic/room.$roomId.reaction", StompReaction::class.java)
                .collectLatest { react ->
                    _newReaction.value = react
                }
        }

        viewModelScope.launch {
            // Subscribe to Presence
            stompManager.subscribe("/topic/room.$roomId.presence", StompPresenceEvent::class.java)
                .collectLatest { event ->
                    _presenceEvent.value = event
                    // Refresh room details to update member count
                    repository.getRoomById(roomId).onSuccess { updatedRoom ->
                        val current = _roomState.value
                        if (current is WatchPartyRoomUiState.Success) {
                            _roomState.value = current.copy(room = updatedRoom)
                        }
                    }
                }
        }
    }

    fun sendMessage(content: String, messageType: String = "CHAT") {
        val roomId = activeRoomId ?: return
        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "content" to content,
                    "messageType" to messageType
                )
                stompManager.sendJson("/app/room.$roomId.chat", payload)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send STOMP message: ${e.message}")
            }
        }
    }

    fun sendSyncCommand(action: String, timestamp: Double) {
        val roomId = activeRoomId ?: return
        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "action" to action,
                    "timestamp" to timestamp
                )
                stompManager.sendJson("/app/room.$roomId.sync", payload)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send STOMP sync command: ${e.message}")
            }
        }
    }

    fun sendReaction(emoji: String) {
        val roomId = activeRoomId ?: return
        viewModelScope.launch {
            try {
                stompManager.sendJson("/app/room.$roomId.reaction", mapOf("emoji" to emoji))
            } catch (e: Exception) {
                Log.e(tag, "Failed to send STOMP reaction: ${e.message}")
            }
        }
    }

    fun leaveRoom() {
        val roomId = activeRoomId ?: return
        viewModelScope.launch {
            repository.leaveRoom(roomId)
            stompManager.disconnect()
            activeRoomId = null
            _roomState.value = WatchPartyRoomUiState.Loading
        }
    }

    fun endRoom() {
        val roomId = activeRoomId ?: return
        viewModelScope.launch {
            repository.endRoom(roomId)
            stompManager.disconnect()
            activeRoomId = null
            _roomState.value = WatchPartyRoomUiState.Loading
        }
    }

    fun loadHistory(onSuccess: (List<WatchPartyHistoryDto>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            repository.getWatchPartyHistory(0, 50)
                .onSuccess { page ->
                    onSuccess(page.content)
                }
                .onFailure { error ->
                    onFailure(ErrorMapper.mapToString(error))
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stompManager.disconnect()
    }
}

sealed class WatchPartyLobbyUiState {
    object Loading : WatchPartyLobbyUiState()
    data class Success(val rooms: List<WatchRoomDto>) : WatchPartyLobbyUiState()
    data class Error(val message: String) : WatchPartyLobbyUiState()
}

sealed class WatchPartyRoomUiState {
    object Loading : WatchPartyRoomUiState()
    data class Success(
        val room: WatchRoomDto,
        val messages: List<ChatMessageDto>,
        val videoState: VideoStateDto,
        val videoUrl: String
    ) : WatchPartyRoomUiState()
    data class Error(val message: String) : WatchPartyRoomUiState()
}

sealed class MoviePickerUiState {
    object Loading : MoviePickerUiState()
    data class Success(val movies: List<MovieDto>) : MoviePickerUiState()
    data class Error(val message: String) : MoviePickerUiState()
}
