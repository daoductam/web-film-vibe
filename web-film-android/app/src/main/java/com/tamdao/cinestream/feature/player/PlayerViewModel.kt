package com.tamdao.cinestream.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.EpisodeDto
import com.tamdao.cinestream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tamdao.cinestream.core.download.DownloadManagerWrapper
import com.tamdao.cinestream.core.database.OfflineEpisodeEntity
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MovieRepository,
    val downloadManagerWrapper: DownloadManagerWrapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _offlineEpisode = MutableStateFlow<OfflineEpisodeEntity?>(null)
    val offlineEpisode: StateFlow<OfflineEpisodeEntity?> = _offlineEpisode.asStateFlow()

    fun loadEpisode(movieSlug: String, episodeSlug: String) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            
            // Check offline episode
            val localEp = repository.getOfflineEpisode(episodeSlug)
            _offlineEpisode.value = localEp
            
            try {
                // Thử tải thông tin từ Network trước
                val response = repository.getMovieDetail(movieSlug)
                if (response.success && response.data != null) {
                    val allEpisodes = response.data.servers.flatMap { it.episodes }
                    val currentEpisode = allEpisodes.find { it.slug == episodeSlug }
                    
                    if (currentEpisode != null) {
                        _uiState.value = PlayerUiState.Success(
                            movieSlug = movieSlug,
                            movieTitle = response.data.title,
                            thumbUrl = response.data.thumbUrl,
                            episode = currentEpisode,
                            allEpisodes = allEpisodes
                        )
                        return@launch
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Tải thông tin từ server thất bại, đang chuyển sang chế độ offline: ${e.message}")
            }
            
            // Nếu mất mạng hoặc API lỗi, thử tải từ Database local lưu trữ ngoại tuyến
            try {
                val localMovie = repository.getOfflineMovie(movieSlug)
                if (localMovie != null) {
                    val gson = com.google.gson.Gson()
                    val typeToken = object : com.google.gson.reflect.TypeToken<List<com.tamdao.cinestream.data.model.ServerEpisodeGroupDto>>() {}.type
                    val servers: List<com.tamdao.cinestream.data.model.ServerEpisodeGroupDto> = gson.fromJson(localMovie.serversJson, typeToken)
                    
                    val allEpisodes = servers.flatMap { it.episodes }
                    val currentEpisode = allEpisodes.find { it.slug == episodeSlug }
                    
                    if (currentEpisode != null) {
                        _uiState.value = PlayerUiState.Success(
                            movieSlug = movieSlug,
                            movieTitle = localMovie.title,
                            thumbUrl = localMovie.thumbUrl ?: "",
                            episode = currentEpisode,
                            allEpisodes = allEpisodes
                        )
                        return@launch
                    }
                }
            } catch (localEx: Exception) {
                android.util.Log.e("PlayerViewModel", "Lỗi đọc dữ liệu local: ${localEx.message}")
            }
            
            _uiState.value = PlayerUiState.Error("Không thể tải tập phim này. Vui lòng kiểm tra kết nối mạng!")
        }
    }

    fun saveProgress(
        movieSlug: String,
        movieTitle: String,
        thumbUrl: String,
        episode: EpisodeDto,
        progressMs: Long,
        durationMs: Long
    ) {
        viewModelScope.launch {
            repository.saveWatchHistory(
                com.tamdao.cinestream.core.database.WatchHistoryEntity(
                    slug = movieSlug,
                    title = movieTitle,
                    thumbUrl = thumbUrl,
                    lastEpisodeName = episode.name,
                    lastEpisodeSlug = episode.slug,
                    progressMs = progressMs,
                    durationMs = durationMs
                )
            )
        }
    }
}

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(
        val movieSlug: String,
        val movieTitle: String,
        val thumbUrl: String,
        val episode: EpisodeDto,
        val allEpisodes: List<EpisodeDto>
    ) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}
