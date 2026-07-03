package com.tamdao.cinestream.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.util.ErrorMapper
import com.tamdao.cinestream.data.model.*
import com.tamdao.cinestream.data.repository.MovieRepository
import com.tamdao.cinestream.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.tamdao.cinestream.core.download.DownloadManagerWrapper
import com.tamdao.cinestream.core.database.OfflineEpisodeEntity
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val socialRepository: SocialRepository,
    private val downloadManagerWrapper: DownloadManagerWrapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    private val _userRating = MutableStateFlow<Int?>(null)
    val userRating: StateFlow<Int?> = _userRating.asStateFlow()

    private val _offlineEpisodes = MutableStateFlow<List<OfflineEpisodeEntity>>(emptyList())
    val offlineEpisodes: StateFlow<List<OfflineEpisodeEntity>> = _offlineEpisodes.asStateFlow()

    private val _similarMovies = MutableStateFlow<List<MovieDto>>(emptyList())
    val similarMovies: StateFlow<List<MovieDto>> = _similarMovies.asStateFlow()

    fun loadMovieDetail(slug: String) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            
            // Observe offline episodes for this movie
            launch {
                repository.getOfflineEpisodesByMovie(slug).collectLatest { eps ->
                    _offlineEpisodes.value = eps
                }
            }
            
            launch {
                try {
                    repository.isFavorite(slug).collectLatest {
                        _isFavorite.value = it
                    }
                } catch (e: Exception) {}
            }

            launch {
                try {
                    repository.getSimilarMovies(slug).collectLatest { similar ->
                        _similarMovies.value = similar
                    }
                } catch (e: Exception) {}
            }

            try {
                val response = repository.getMovieDetail(slug)
                if (response.success && response.data != null) {
                    _uiState.value = MovieDetailUiState.Success(response.data)
                    // Cập nhật/đồng bộ thông tin chi tiết phim vào Room DB
                    repository.saveMovieOffline(response.data)
                    loadUserRating(slug)
                } else {
                    // Thử tải từ DB local lưu trữ ngoại tuyến
                    val localDetail = repository.getOfflineMovieDetail(slug)
                    if (localDetail != null) {
                        _uiState.value = MovieDetailUiState.Success(localDetail)
                    } else {
                        _uiState.value = MovieDetailUiState.Error("Không thể tải thông tin phim. Vui lòng thử lại.")
                    }
                }
            } catch (e: Exception) {
                // Thử tải từ DB local lưu trữ ngoại tuyến
                val localDetail = repository.getOfflineMovieDetail(slug)
                if (localDetail != null) {
                    _uiState.value = MovieDetailUiState.Success(localDetail)
                } else {
                    _uiState.value = MovieDetailUiState.Error(ErrorMapper.mapToString(e))
                }
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

    fun loadComments(movieSlug: String) {
        viewModelScope.launch {
            try {
                val response = socialRepository.getMovieComments(movieSlug)
                if (response.success && response.data != null) {
                    _comments.value = response.data.content
                }
            } catch (e: Exception) {}
        }
    }

    fun addComment(movieSlug: String, episodeSlug: String, content: String, parentId: Long? = null) {
        viewModelScope.launch {
            try {
                val response = socialRepository.addComment(movieSlug, episodeSlug, content, parentId)
                if (response.success && response.data != null) {
                    loadComments(movieSlug)
                }
            } catch (e: Exception) {}
        }
    }

    fun toggleLike(commentId: Long, movieSlug: String) {
        viewModelScope.launch {
            try {
                val response = socialRepository.toggleLike(commentId)
                if (response.success) {
                    loadComments(movieSlug)
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

    fun downloadEpisode(movie: MovieDetailDto, episode: EpisodeDto) {
        viewModelScope.launch {
            try {
                // 1. Lưu metadata vào DB offline nếu chưa có
                repository.saveMovieOffline(movie)
                
                // 2. Lưu thông tin tập phim offline với composite ID để tránh xung đột
                val uniqueEpisodeSlug = "${movie.slug}_${episode.slug}"
                repository.saveEpisodeOffline(
                    episodeSlug = uniqueEpisodeSlug,
                    movieSlug = movie.slug,
                    episodeName = episode.name,
                    videoUrl = episode.linkM3u8 ?: ""
                )
                
                // 3. Gọi DownloadManager của Media3 qua Wrapper
                episode.linkM3u8?.let { videoUrl ->
                    downloadManagerWrapper.startDownload(uniqueEpisodeSlug, videoUrl)
                    println("Bắt đầu tải tập phim: ${episode.name} của ${movie.title} từ $videoUrl")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class MovieDetailUiState {
    object Loading : MovieDetailUiState()
    data class Success(val movie: MovieDetailDto) : MovieDetailUiState()
    data class Error(val message: String) : MovieDetailUiState()
}
