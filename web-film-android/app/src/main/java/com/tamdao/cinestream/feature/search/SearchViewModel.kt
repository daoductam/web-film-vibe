package com.tamdao.cinestream.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear = _selectedYear.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Debounce search: Tối ưu hóa việc gọi API khi người dùng gõ phím
        _searchQuery
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { query ->
                performSearch(query, _selectedCategory.value, _selectedYear.value)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
        // Khi gõ chữ, nếu đang có một Thể loại được chọn, chúng ta vẫn ưu tiên Tìm kiếm từ khóa
        // Nhưng không xóa đi để người dùng biết họ đang ở filter nào
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
        // Khi chọn thể loại, nếu có chữ trong ô search, chúng ta ưu tiên Search từ khóa
        // Nếu ô search trống, chúng ta Filter theo thể loại
        performSearch(_searchQuery.value, category, _selectedYear.value)
    }

    fun onYearSelected(year: Int?) {
        _selectedYear.value = year
        performSearch(_searchQuery.value, _selectedCategory.value, year)
    }

    private fun performSearch(query: String, category: String? = null, year: Int? = null) {
        // Nếu tất cả đều trống thì quay về trạng thái Idle
        if (query.isEmpty() && category == null && year == null) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                // MovieRepository.getMoviesByFilter sẽ ưu tiên Search nếu query != null
                val response = repository.getMoviesByFilter(
                    query = if (query.trim().length >= 2) query.trim() else null,
                    categories = if (query.isEmpty() && category != null) listOf(category) else null, // Ưu tiên query hơn category
                    year = if (query.isEmpty()) year else null
                )
                
                if (response.success && response.data != null) {
                    _uiState.value = SearchUiState.Success(response.data.content)
                } else {
                    _uiState.value = SearchUiState.Error(response.message ?: "Không tìm thấy kết quả")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<MovieDto>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
