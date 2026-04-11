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
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadEpisode(movieSlug: String, episodeSlug: String) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            try {
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
                    } else {
                        _uiState.value = PlayerUiState.Error("Episode not found")
                    }
                } else {
                    _uiState.value = PlayerUiState.Error("Movie not found")
                }
            } catch (e: Exception) {
                _uiState.value = PlayerUiState.Error(e.message ?: "Failed to load")
            }
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
