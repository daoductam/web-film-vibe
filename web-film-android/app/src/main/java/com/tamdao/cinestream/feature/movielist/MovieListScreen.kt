package com.tamdao.cinestream.feature.movielist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tamdao.cinestream.core.components.CineMovieCard
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    title: String,
    onMovieClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: MovieListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
            )
        },
        bottomBar = {
            if (uiState is MovieListUiState.Success) {
                val state = uiState as MovieListUiState.Success
                PaginationControls(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    onPrevClick = { viewModel.prevPage() },
                    onNextClick = { viewModel.nextPage() }
                )
            }
        },
        containerColor = Obsidian
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val categories by viewModel.categories.collectAsState()
            val selectedCategories by viewModel.selectedCategories.collectAsState()

            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategories.contains(category.slug),
                            onClick = { viewModel.toggleCategory(category.slug) },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Obsidian
                            )
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is MovieListUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                    }
                    is MovieListUiState.Success -> {
                        if (state.movies.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Không tìm thấy phim nào.", color = Color.Gray)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(bottom = 16.dp, start = 8.dp, end = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.movies) { movie ->
                                    CineMovieCard(movie = movie, onClick = onMovieClick)
                                }
                            }
                        }
                    }
                    is MovieListUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                            Button(
                                onClick = { viewModel.loadMovies(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text("Thử lại", color = Obsidian)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        color = Obsidian,
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(width = 1.dp, color = Color.Gray.copy(alpha = 0.2f)) // Thêm viền để tách biệt
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrevClick,
                enabled = currentPage > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage > 0) Color.DarkGray else Color.Transparent,
                    contentColor = if (currentPage > 0) NeonCyan else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(text = "Trang trước", fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }

            Button(
                onClick = onNextClick,
                enabled = currentPage < totalPages - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage < totalPages - 1) Color.DarkGray else Color.Transparent,
                    contentColor = if (currentPage < totalPages - 1) NeonCyan else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(text = "Trang sau", fontWeight = FontWeight.Bold)
            }
        }
    }
}
