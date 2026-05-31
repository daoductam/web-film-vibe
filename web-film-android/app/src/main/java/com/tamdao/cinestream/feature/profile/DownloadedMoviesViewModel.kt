package com.tamdao.cinestream.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.database.OfflineEpisodeEntity
import com.tamdao.cinestream.core.database.OfflineMovieEntity
import com.tamdao.cinestream.data.repository.MovieRepository
import com.tamdao.cinestream.core.download.DownloadManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadedMoviesViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val downloadManagerWrapper: DownloadManagerWrapper
) : ViewModel() {

    // Lấy tất cả phim đã lưu thông tin offline
    val offlineMovies: StateFlow<List<OfflineMovieEntity>> = repository.getAllOfflineMovieEntities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Lấy tất cả tập phim đã lưu offline
    val offlineEpisodes: StateFlow<List<OfflineEpisodeEntity>> = repository.getOfflineEpisodesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tổ hợp dữ liệu phim kèm danh sách các tập đã tải xong
    val downloadedMoviesState: StateFlow<List<DownloadedMovieState>> = combine(
        offlineMovies,
        offlineEpisodes
    ) { movies, episodes ->
        movies.map { movie ->
            val movieEps = episodes.filter { it.movieSlug == movie.slug && it.downloadStatus == "COMPLETED" }
            DownloadedMovieState(
                movie = movie,
                episodes = movieEps
            )
        }.filter { it.episodes.isNotEmpty() } // Chỉ hiển thị phim có ít nhất 1 tập tải xong
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Xóa tập phim offline
    fun deleteEpisode(episode: OfflineEpisodeEntity) {
        viewModelScope.launch {
            // 1. Hủy tải xuống/xóa file từ ExoPlayer DownloadManager
            downloadManagerWrapper.cancelDownload(episode.episodeSlug)
            
            // 2. Xóa khỏi Room DB
            repository.deleteOfflineEpisode(episode.episodeSlug)
            
            // 3. Nếu phim không còn tập nào đã tải, xóa luôn thông tin phim offline
            val remaining = repository.getOfflineEpisodesByMovie(episode.movieSlug).stateIn(viewModelScope).value
            if (remaining.isEmpty()) {
                repository.deleteOfflineMovieAndEpisodes(episode.movieSlug)
            }
        }
    }
}

data class DownloadedMovieState(
    val movie: OfflineMovieEntity,
    val episodes: List<OfflineEpisodeEntity>
)
