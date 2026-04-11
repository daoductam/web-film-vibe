package com.tamdao.cinestream.feature.detail

import com.tamdao.cinestream.data.model.MovieDetailDto
import com.tamdao.cinestream.data.model.EpisodeDto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    slug: String,
    onBackClick: () -> Unit,
    onPlayClick: (String, String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(slug) {
        viewModel.loadMovieDetail(slug)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phim", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    // Nút Yêu thích (Library)
                    val movie = (uiState as? MovieDetailUiState.Success)?.movie
                    if (movie != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(movie) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) NeonCyan else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
            )
        },
        containerColor = Obsidian
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is MovieDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonCyan)
                }
                is MovieDetailUiState.Success -> {
                    val movie = state.movie
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            AsyncImage(
                                model = movie.posterUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(250.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = movie.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text(text = "${movie.year} • ${movie.quality} • ${movie.duration}", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = movie.description ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(text = "Danh sách tập phim", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Hiển thị tập phim từ server đầu tiên
                        item {
                            val episodes = movie.servers.firstOrNull()?.episodes ?: emptyList()
                            Column(modifier = Modifier.padding(horizontal = 16.dp).heightIn(max = 1000.dp)) {
                                episodes.chunked(4).forEach { rowEps ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowEps.forEach { ep ->
                                            Button(
                                                onClick = { onPlayClick(movie.slug, ep.slug) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(8.dp)
                                            ) {
                                                Text(text = ep.name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                                            }
                                        }
                                        // Empty spaces to fill the row
                                        repeat(4 - rowEps.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(50.dp)) }
                    }
                }
                is MovieDetailUiState.Error -> {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
