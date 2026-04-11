package com.tamdao.cinestream.feature.search

import com.tamdao.cinestream.data.model.MovieDto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tamdao.cinestream.core.components.CineMovieCard
import com.tamdao.cinestream.core.components.MovieListShimmer
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        // Search Bar
        Row(
            modifier = Modifier.statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Bạn muốn xem phim gì?", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = NeonCyan,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                singleLine = true
            )
        }

        // Filter Quick Chips (Đã chuyển sang LazyRow để cuộn ngang và sửa lỗi tràn màu)
        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategoryClick = { viewModel.onCategorySelected(it) }
        )

        // Results
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Khám phá hàng ngàn bộ phim", color = Color.Gray)
                    }
                }
                is SearchUiState.Loading -> {
                    // Sử dụng Shimmer thay cho Spinner
                    Column(modifier = Modifier.fillMaxSize()) {
                        repeat(3) {
                            MovieListShimmer()
                        }
                    }
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(
                            "Không tìm thấy phim nào phù hợp",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results) { movie ->
                                Box(modifier = Modifier.padding(4.dp)) {
                                    CineMovieCard(movie = movie, onClick = onMovieClick)
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedCategory: String?,
    onCategoryClick: (String?) -> Unit
) {
    // Danh sách thể loại đầy đủ và phong phú hơn
    val categories = listOf(
        "Hành động" to "hanh-dong",
        "Cổ trang" to "co-trang",
        "Võ thuật" to "vo-thuat",
        "Hài hước" to "hai-huoc",
        "Kinh dị" to "kinh-di",
        "Tình cảm" to "tinh-cam",
        "Tâm lý" to "tam-ly",
        "Hoạt hình" to "hoat-hinh",
        "Khoa học" to "khoa-hoc-vien-tuong",
        "Viễn tưởng" to "vien-tuong",
        "Hình sự" to "hinh-su",
        "Chiến tranh" to "chien-tranh",
        "Phiêu lưu" to "phieu-luu",
        "Âm nhạc" to "am-nhac",
        "Thần thoại" to "than-thoai",
        "Tài liệu" to "tai-lieu",
        "Gia đình" to "gia-dinh"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (name, slug) ->
            val isSelected = selectedCategory == slug
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryClick(if (isSelected) null else slug) },
                label = { Text(name, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = Obsidian,
                    containerColor = Color.White.copy(alpha = 0.1f),
                    labelColor = Color.White
                ),
                border = null,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
