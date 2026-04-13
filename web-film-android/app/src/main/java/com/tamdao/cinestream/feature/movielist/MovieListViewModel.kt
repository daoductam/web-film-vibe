package com.tamdao.cinestream.feature.movielist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val title: String = savedStateHandle["title"] ?: ""
    private val type: String = savedStateHandle["type"] ?: ""
    private val category: String? = savedStateHandle["category"]

    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<com.tamdao.cinestream.data.model.CategoryDto>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()

    init {
        if (category != null && category != "null" && category.isNotEmpty()) {
            _selectedCategories.value = setOf(category)
        }
        loadCategories()
        loadMovies(0)
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = repository.getCategories()
        }
    }
    
    fun toggleCategory(categorySlug: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(categorySlug)) {
            current.remove(categorySlug)
        } else {
            current.add(categorySlug) // Or allow only 1? User requested "nhiều thể loại", so Set is correct.
        }
        _selectedCategories.value = current
        loadMovies(0)
    }

    fun loadMovies(page: Int) {
        _uiState.value = MovieListUiState.Loading
        viewModelScope.launch {
            try {
                val cats = _selectedCategories.value.toList().ifEmpty { null }
                android.util.Log.d("MovieList", "Loading movies: type=$type, categories=$cats, page=$page")
                val response = repository.getMoviesByFilter(
                    type = if (type == "null" || type.isEmpty()) null else type,
                    categories = cats,
                    page = page
                )

                android.util.Log.d("MovieList", "Response: success=${response.success}, data=${response.data != null}")
                if (response.success && response.data != null) {
                    val pageData = response.data
                    val safeTotalPages = response.pagination?.totalPages 
                        ?: pageData.page?.totalPages 
                        ?: pageData.totalPages
                        
                    val safeCurrentPage = response.pagination?.currentPage 
                        ?: pageData.page?.number 
                        ?: pageData.number

                    android.util.Log.d("MovieList", "Movies: count=${pageData.content.size}, totalPages=${safeTotalPages}, page=${safeCurrentPage}")
                    _uiState.value = MovieListUiState.Success(
                        title = title,
                        movies = pageData.content,
                        currentPage = safeCurrentPage,
                        totalPages = safeTotalPages
                    )
                } else {
                    android.util.Log.e("MovieList", "Error: ${response.message}")
                    _uiState.value = MovieListUiState.Error(response.message ?: "Lỗi tải phim")
                }
            } catch (e: Exception) {
                android.util.Log.e("MovieList", "Exception: ${e.message}", e)
                _uiState.value = MovieListUiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun nextPage() {
        val currentState = _uiState.value
        if (currentState is MovieListUiState.Success) {
            if (currentState.currentPage < currentState.totalPages - 1) {
                loadMovies(currentState.currentPage + 1)
            }
        }
    }

    fun prevPage() {
        val currentState = _uiState.value
        if (currentState is MovieListUiState.Success) {
            if (currentState.currentPage > 0) {
                loadMovies(currentState.currentPage - 1)
            }
        }
    }
    
    fun goToPage(page: Int) {
        loadMovies(page)
    }
}

sealed class MovieListUiState {
    object Loading : MovieListUiState()
    data class Success(
        val title: String,
        val movies: List<MovieDto>,
        val currentPage: Int,
        val totalPages: Int
    ) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}
