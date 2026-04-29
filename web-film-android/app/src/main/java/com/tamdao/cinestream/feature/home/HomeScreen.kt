package com.tamdao.cinestream.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
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

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tamdao.cinestream.feature.ai_chat.AiChatBottomSheet

@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onSeeAllClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    var showAiChat by remember { mutableStateOf(false) }

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

                    item { 
                        MovieSection(
                            title = "Phim Mới Cập Nhật", 
                            movies = state.latestMovies, 
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { onSeeAllClick("Phim Mới", "latest") }
                        ) 
                    }
                    if (state.seriesMovies.isNotEmpty()) {
                        item { 
                            MovieSection(
                                title = "Phim Bộ Đặc Sắc", 
                                movies = state.seriesMovies, 
                                onMovieClick = onMovieClick,
                                onSeeAllClick = { onSeeAllClick("Phim Bộ", "series") }
                            ) 
                        }
                    }
                    if (state.singleMovies.isNotEmpty()) {
                        item { 
                            MovieSection(
                                title = "Phim Lẻ Mới Nhất", 
                                movies = state.singleMovies, 
                                onMovieClick = onMovieClick,
                                onSeeAllClick = { onSeeAllClick("Phim Lẻ", "single") }
                            ) 
                        }
                    }
                    if (state.animationMovies.isNotEmpty()) {
                        item { 
                            MovieSection(
                                title = "Hoạt Hình & Anime", 
                                movies = state.animationMovies, 
                                onMovieClick = onMovieClick,
                                onSeeAllClick = { onSeeAllClick("Hoạt Hình", "hoathinh") }
                            ) 
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
            is HomeUiState.Error -> {
                HomeErrorScreen(state.message.asString())
            }
        }

        // AI Chat FAB
        FloatingActionButton(
            onClick = { showAiChat = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = NeonCyan,
            contentColor = Obsidian
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI CineGuru"
            )
        }
        
        if (showAiChat) {
            AiChatBottomSheet(
                onDismissRequest = { showAiChat = false },
                onMovieClick = { slug -> 
                    showAiChat = false
                    onMovieClick(slug)
                }
            )
        }
    }
}

@Composable
fun CineHomeHeader(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Obsidian.copy(alpha = 0.9f),
                        Obsidian.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo CineStream
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = {}) // Ripple effect for logo if needed
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonCyan, Color(0xFF00BFA5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Obsidian,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CINESTREAM",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontSize = 20.sp
                )
            }

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
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
fun MovieSection(
    title: String, 
    movies: List<com.tamdao.cinestream.data.model.MovieDto>, 
    onMovieClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Xem tất cả",
                color = NeonCyan,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
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
