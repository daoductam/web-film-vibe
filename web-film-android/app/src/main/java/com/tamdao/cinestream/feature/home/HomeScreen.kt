package com.tamdao.cinestream.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tamdao.cinestream.core.components.CineHeroSection
import com.tamdao.cinestream.core.components.CineMovieCard
import com.tamdao.cinestream.core.components.MovieListShimmer
import com.tamdao.cinestream.core.database.WatchHistoryEntity
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { CineHomeHeader(onSearchClick) }
                    repeat(3) {
                        item { MovieListShimmer() }
                    }
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 1. Hero Section
                    item {
                        Box {
                            CineHeroSection(
                                movie = state.heroMovie,
                                onWatchClick = onMovieClick
                            )
                            CineHomeHeader(onSearchClick)
                        }
                    }

                    // 2. Phim đang xem
                    if (watchHistory.isNotEmpty()) {
                        item {
                            WatchHistorySection(
                                history = watchHistory,
                                onMovieClick = onMovieClick
                            )
                        }
                    }

                    item { MovieSection("Phim Mới Cập Nhật", state.latestMovies, onMovieClick) }
                    if (state.seriesMovies.isNotEmpty()) {
                        item { MovieSection("Phim Bộ Đặc Sắc", state.seriesMovies, onMovieClick) }
                    }
                    if (state.singleMovies.isNotEmpty()) {
                        item { MovieSection("Phim Lẻ Mới Nhất", state.singleMovies, onMovieClick) }
                    }
                    if (state.animationMovies.isNotEmpty()) {
                        item { MovieSection("Hoạt Hình & Anime", state.animationMovies, onMovieClick) }
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
            is HomeUiState.Error -> {
                HomeErrorScreen(state.message)
            }
        }
    }
}

@Composable
fun CineHomeHeader(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo CineStream
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "S", color = Obsidian, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CINESTREAM",
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 18.sp
            )
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun WatchHistorySection(history: List<WatchHistoryEntity>, onMovieClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Tiếp tục xem",
            color = NeonCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { item -> WatchHistoryCard(item, onMovieClick) }
        }
    }
}

@Composable
fun WatchHistoryCard(item: WatchHistoryEntity, onClick: (String) -> Unit) {
    Column(modifier = Modifier.width(200.dp).clickable { onClick(item.slug) }) {
        Box(modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
            AsyncImage(model = item.thumbUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            val progress = if (item.durationMs > 0) item.progressMs.toFloat() / item.durationMs else 0f
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomStart).background(Color.Gray.copy(alpha = 0.5f))) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(NeonCyan))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = "Đang xem: ${item.lastEpisodeName}", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun MovieSection(title: String, movies: List<com.tamdao.cinestream.data.model.MovieDto>, onMovieClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(movies) { movie -> CineMovieCard(movie = movie, onClick = onMovieClick) }
        }
    }
}

@Composable
fun HomeErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Có lỗi xảy ra", color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = message, color = Color.Gray, fontSize = 14.sp)
        }
    }
}
