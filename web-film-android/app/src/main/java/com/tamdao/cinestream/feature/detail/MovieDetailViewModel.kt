package com.tamdao.cinestream.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.MovieDetailDto
import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    // Theo dõi trạng thái yêu thích của phim hiện tại
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadMovieDetail(slug: String) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            
            // 1. Theo dõi trạng thái yêu thích từ Local DB (Chạy trong coroutine riêng để không chặn API)
            launch {
                try {
                    repository.isFavorite(slug).collectLatest {
                        _isFavorite.value = it
                    }
                } catch (e: Exception) {}
            }

            // 2. Tải chi tiết phim từ API
            try {
                val response = repository.getMovieDetail(slug)
                if (response.success && response.data != null) {
                    _uiState.value = MovieDetailUiState.Success(response.data)
                } else {
                    _uiState.value = MovieDetailUiState.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailUiState.Error(e.message ?: "Connection failed")
            }
        }
    }

    fun toggleFavorite(movie: MovieDetailDto) {
        viewModelScope.launch {
            val dto = MovieDto(
                id = movie.id,
                title = movie.title,
                originTitle = movie.originTitle,
                slug = movie.slug,
                thumbUrl = movie.thumbUrl,
                posterUrl = movie.posterUrl,
                year = movie.year,
                type = movie.type ?: "MOVIE",
                quality = movie.quality,
                currentEpisode = movie.currentEpisode,
                language = movie.language,
                viewCount = movie.viewCount
            )
            repository.toggleFavorite(dto)
        }
    }
}

sealed class MovieDetailUiState {
    object Loading : MovieDetailUiState()
    data class Success(val movie: MovieDetailDto) : MovieDetailUiState()
    data class Error(val message: String) : MovieDetailUiState()
}
