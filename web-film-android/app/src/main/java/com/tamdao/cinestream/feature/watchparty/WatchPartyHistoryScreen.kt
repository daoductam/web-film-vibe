package com.tamdao.cinestream.feature.watchparty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tamdao.cinestream.data.model.WatchPartyHistoryDto
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchPartyHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: WatchPartyViewModel = hiltViewModel()
) {
    var historyList by remember { mutableStateOf<List<WatchPartyHistoryDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(
            onSuccess = {
                historyList = it
                loading = false
            },
            onFailure = {
                loading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử Watch Party", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
            )
        },
        containerColor = Obsidian
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Obsidian)
        ) {
            if (loading) {
                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.align(Alignment.Center))
            } else if (historyList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chưa tham gia phòng xem chung nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList) { item ->
                        HistoryCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: WatchPartyHistoryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Phòng: ${item.roomName}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Phim đã xem: ${item.movieTitle}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val durationMin = item.watchDurationSeconds / 60
            Text(
                text = "Thời gian xem: $durationMin phút (${item.watchDurationSeconds} giây)",
                color = NeonCyan,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tham gia lúc: ${item.joinedAt.substringBefore("T")}",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
