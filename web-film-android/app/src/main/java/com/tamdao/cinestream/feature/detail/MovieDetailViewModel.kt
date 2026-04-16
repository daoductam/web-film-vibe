package com.tamdao.cinestream.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.*
import com.tamdao.cinestream.data.repository.MovieRepository
import com.tamdao.cinestream.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    private val _userRating = MutableStateFlow<Int?>(null)
    val userRating: StateFlow<Int?> = _userRating.asStateFlow()

    fun loadMovieDetail(slug: String) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            
            launch {
                try {
                    repository.isFavorite(slug).collectLatest {
                        _isFavorite.value = it
                    }
                } catch (e: Exception) {}
            }

            try {
                val response = repository.getMovieDetail(slug)
                if (response.success && response.data != null) {
                    _uiState.value = MovieDetailUiState.Success(response.data)
                    loadUserRating(slug)
                } else {
                    _uiState.value = MovieDetailUiState.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailUiState.Error(e.message ?: "Connection failed")
            }
        }
    }

    private fun loadUserRating(slug: String) {
        viewModelScope.launch {
            try {
                val response = socialRepository.getMovieRating(slug)
                if (response.success) {
                    _userRating.value = response.data?.userRating
                }
            } catch (e: Exception) {}
        }
    }

    fun loadComments(episodeSlug: String) {
        viewModelScope.launch {
            try {
                val response = socialRepository.getComments(episodeSlug)
                if (response.success && response.data != null) {
                    _comments.value = response.data.content
                }
            } catch (e: Exception) {}
        }
    }

    fun addComment(movieSlug: String?, episodeSlug: String, content: String, parentId: Long? = null) {
        viewModelScope.launch {
            try {
                val response = socialRepository.addComment(movieSlug, episodeSlug, content, parentId)
                if (response.success && response.data != null) {
                    loadComments(episodeSlug)
                }
            } catch (e: Exception) {}
        }
    }

    fun toggleLike(commentId: Long, episodeSlug: String) {
        viewModelScope.launch {
            try {
                val response = socialRepository.toggleLike(commentId)
                if (response.success) {
                    loadComments(episodeSlug)
                }
            } catch (e: Exception) {}
        }
    }

    fun submitRating(movieSlug: String, score: Int) {
        viewModelScope.launch {
            try {
                val response = socialRepository.submitRating(movieSlug, score)
                if (response.success) {
                    _userRating.value = score
                    loadMovieDetail(movieSlug)
                }
            } catch (e: Exception) {}
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
