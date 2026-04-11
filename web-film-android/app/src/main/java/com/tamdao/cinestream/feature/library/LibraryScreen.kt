package com.tamdao.cinestream.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import com.tamdao.cinestream.core.components.CineMovieCard
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@Composable
fun LibraryScreen(
    onMovieClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        // Header
        Text(
            text = "Phim Yêu Thích",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
        )

        Divider(color = Color.White.copy(alpha = 0.1f))

        if (favorites.isEmpty()) {
            EmptyLibraryMessage()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites) { movie ->
                    Box(modifier = Modifier.padding(4.dp)) {
                        CineMovieCard(movie = movie, onClick = onMovieClick)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Thư viện trống",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hãy thả tim cho bộ phim bạn yêu thích!",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = NeonCyan.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.padding(24.dp).size(48.dp)
                )
            }
        }
    }
}
