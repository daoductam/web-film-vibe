package com.tamdao.cinestream.feature.detail

import com.tamdao.cinestream.data.model.MovieDetailDto
import com.tamdao.cinestream.data.model.EpisodeDto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import com.tamdao.cinestream.ui.theme.SurfaceDark
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

import com.tamdao.cinestream.feature.detail.components.CommentSection
import com.tamdao.cinestream.feature.detail.components.StarRatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    slug: String,
    onBackClick: () -> Unit,
    onPlayClick: (String, String) -> Unit,
    onMovieClick: (String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val userRating by viewModel.userRating.collectAsState()
    val offlineEpisodes by viewModel.offlineEpisodes.collectAsState()
    val similarMovies by viewModel.similarMovies.collectAsState()
    var showDownloadSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showOfflineDialog by remember { mutableStateOf(false) }

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
                    val movie = (uiState as? MovieDetailUiState.Success)?.movie
                    if (movie != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(movie) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) NeonCyan else Color.White
                            )
                        }
                        IconButton(onClick = { showDownloadSheet = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White
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
                    
                    // Load movie-wide comments once
                    LaunchedEffect(movie.slug) {
                        viewModel.loadComments(movie.slug)
                    }

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
                                
                                Spacer(modifier = Modifier.height(8.dp))

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Rating Bar
                                StarRatingBar(
                                    rating = movie.averageRating ?: 0.0,
                                    count = movie.ratingCount ?: 0L,
                                    userRating = userRating,
                                    onRate = { score -> viewModel.submitRating(movie.slug, score) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "${movie.year} • ${movie.quality} • ${movie.duration}", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = movie.description ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(text = "Danh sách tập phim", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        item {
                            val episodes = movie.servers.firstOrNull()?.episodes ?: emptyList()
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                episodes.chunked(4).forEach { rowEps ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowEps.forEach { ep ->
                                            Button(
                                                onClick = { 
                                                    val isOnline = com.tamdao.cinestream.core.util.NetworkUtils.isNetworkAvailable(context)
                                                    val isDownloaded = offlineEpisodes.any { it.episodeSlug == "${movie.slug}_${ep.slug}" && it.downloadStatus == "COMPLETED" }
                                                    if (isOnline || isDownloaded) {
                                                        onPlayClick(movie.slug, ep.slug)
                                                    } else {
                                                        showOfflineDialog = true
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(8.dp)
                                            ) {
                                                Text(text = ep.name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                                            }
                                        }
                                        repeat(4 - rowEps.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // Similar Movies Section
                        if (similarMovies.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "Phim tương tự",
                                        color = NeonCyan,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(similarMovies) { sim ->
                                            Column(
                                                modifier = Modifier
                                                    .width(110.dp)
                                                    .clickable { onMovieClick(sim.slug) }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(160.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.DarkGray)
                                                ) {
                                                    AsyncImage(
                                                        model = sim.posterUrl ?: sim.thumbUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = sim.title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Comment Section
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            // We still use currentEpisode for context when posting new comments
                            val firstEpisodeSlug = movie.servers.firstOrNull()?.episodes?.firstOrNull()?.slug ?: ""
                            if (movie.slug.isNotEmpty()) {
                                CommentSection(
                                    comments = comments,
                                    onLikeClick = { id -> viewModel.toggleLike(id, movie.slug) },
                                    onReplyClick = { /* Show reply dialog */ },
                                    onDeleteClick = { /* Not used in this simplified UI */ },
                                    onSendComment = { content -> 
                                        viewModel.addComment(movie.slug, firstEpisodeSlug, content)
                                    }
                                )
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

    if (showDownloadSheet && uiState is MovieDetailUiState.Success) {
        val movie = (uiState as MovieDetailUiState.Success).movie
        val episodes = movie.servers.firstOrNull()?.episodes ?: emptyList()
        val sheetState = rememberModalBottomSheetState()
        
        ModalBottomSheet(
            onDismissRequest = { showDownloadSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Tải tập phim",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (episodes.isEmpty()) {
                    Text(
                        text = "Không có tập phim nào để tải.",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        items(episodes.size) { index ->
                            val ep = episodes[index]
                            val offlineEp = offlineEpisodes.find { it.episodeSlug == "${movie.slug}_${ep.slug}" }
                            
                            val isDownloaded = offlineEp?.downloadStatus == "COMPLETED"
                            val isDownloading = offlineEp?.downloadStatus == "DOWNLOADING"
                            val isFailed = offlineEp?.downloadStatus == "FAILED"
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isDownloaded) NeonCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !isDownloaded && !isDownloading) {
                                        viewModel.downloadEpisode(movie, ep)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ep.name,
                                        color = if (isDownloaded) NeonCyan else Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    if (isDownloading) {
                                        Text(
                                            text = "Đang tải: ${offlineEp?.progress?.toInt() ?: 0}%",
                                            color = NeonCyan.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    } else if (isDownloaded) {
                                        Text(
                                            text = "Đã tải xong",
                                            color = NeonCyan,
                                            fontSize = 12.sp
                                        )
                                    } else if (isFailed) {
                                        Text(
                                            text = "Tải thất bại. Nhấn để thử lại.",
                                            color = Color.Red,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        progress = { (offlineEp?.progress ?: 0f) / 100f },
                                        modifier = Modifier.size(24.dp),
                                        color = NeonCyan,
                                        trackColor = Color.White.copy(alpha = 0.2f),
                                        strokeWidth = 3.dp
                                    )
                                } else if (isDownloaded) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Downloaded",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            title = { Text("Không có kết nối mạng", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Tập phim này chưa được tải xuống. Vui lòng kết nối mạng để xem tập phim này.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { showOfflineDialog = false }) {
                    Text("Đóng", color = NeonCyan)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
