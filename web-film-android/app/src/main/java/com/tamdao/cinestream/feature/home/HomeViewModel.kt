package com.tamdao.cinestream.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.core.database.WatchHistoryEntity
import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistoryEntity>> = _watchHistory.asStateFlow()

    init {
        loadHomeData()
        loadWatchHistory()
    }

    private fun loadWatchHistory() {
        viewModelScope.launch {
            repository.getWatchHistory().collectLatest { history ->
                _watchHistory.value = history
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.getLatestMovies()
                .catch { e ->
                    _uiState.value = HomeUiState.Error("Lỗi kết nối: ${e.message}")
                }
                .collectLatest { latest ->
                    if (latest.isNotEmpty()) {
                        // Lấy thêm các danh mục khác song song
                        val series = repository.getMoviesByType("series")
                        val singles = repository.getMoviesByType("single")
                        val hoathinh = repository.getMoviesByType("hoathinh")

                        _uiState.value = HomeUiState.Success(
                            heroMovie = latest.first(),
                            latestMovies = latest.drop(1),
                            seriesMovies = series,
                            singleMovies = singles,
                            animationMovies = hoathinh
                        )
                    } else {
                        _uiState.value = HomeUiState.Error("Không có dữ liệu phim.")
                    }
                }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val heroMovie: MovieDto,
        val latestMovies: List<MovieDto>,
        val seriesMovies: List<MovieDto> = emptyList(),
        val singleMovies: List<MovieDto> = emptyList(),
        val animationMovies: List<MovieDto> = emptyList()
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
