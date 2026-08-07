package com.tamdao.cinestream.feature.watchparty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tamdao.cinestream.data.model.CreateWatchRoomRequest
import com.tamdao.cinestream.data.model.MovieDto
import com.tamdao.cinestream.data.model.WatchRoomDto
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchPartyLobbyScreen(
    onBackClick: () -> Unit,
    onNavigateToRoom: (Long) -> Unit,
    preSelectedMovieId: Long? = null,
    preSelectedMovieTitle: String? = null,
    preSelectedMoviePoster: String? = null,
    viewModel: WatchPartyViewModel = hiltViewModel()
) {
    val lobbyState by viewModel.lobbyState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showMoviePicker by remember { mutableStateOf(false) }
    var joinRoomCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedMovie by remember { mutableStateOf<MovieDto?>(null) }

    // Tự động mở hộp thoại tạo phòng nếu điều hướng từ trang chi tiết phim (Pre-fill params)
    LaunchedEffect(preSelectedMovieId) {
        if (preSelectedMovieId != null) {
            selectedMovie = MovieDto(
                id = preSelectedMovieId,
                title = preSelectedMovieTitle ?: "Phim đã chọn",
                posterUrl = preSelectedMoviePoster,
                thumbUrl = preSelectedMoviePoster
            )
            showCreateDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadPublicRooms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watch Party - Xem Chung", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showMoviePicker = true },
                containerColor = NeonCyan,
                contentColor = Obsidian
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo phòng mới")
            }
        },
        containerColor = Obsidian
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Obsidian)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero / Intro Section
                item {
                    WatchPartyHeroSection()
                }

                // 2. Quick Start Steps
                item {
                    WatchPartyStepsSection()
                }

                // 3. Join Room by Code Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tham gia bằng mã phòng",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = joinRoomCode,
                                    onValueChange = { joinRoomCode = it.uppercase() },
                                    placeholder = { Text("Mã phòng ví dụ: ABC12XYZ", color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (joinRoomCode.isNotBlank()) {
                                            viewModel.joinRoomByCode(
                                                code = joinRoomCode,
                                                onSuccess = { room -> onNavigateToRoom(room.id) },
                                                onFailure = { err -> errorMessage = err }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Text("Vào", color = Obsidian, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 4. Header list public rooms
                item {
                    Text(
                        text = "Các phòng công khai đang hoạt động",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 5. Public Rooms List / Loading / Empty State
                when (val state = lobbyState) {
                    is WatchPartyLobbyUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = NeonCyan)
                            }
                        }
                    }
                    is WatchPartyLobbyUiState.Success -> {
                        if (state.rooms.isEmpty()) {
                            item {
                                WatchPartyEmptyState(onCreateClick = { showMoviePicker = true })
                            }
                        } else {
                            items(state.rooms) { room ->
                                PublicRoomCard(room = room, onJoinClick = { onNavigateToRoom(room.id) })
                            }
                        }
                    }
                    is WatchPartyLobbyUiState.Error -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = state.message, color = Color.Red)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // Error dialog
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    confirmButton = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("Đồng ý", color = NeonCyan)
                        }
                    },
                    title = { Text("Lỗi", color = Color.White) },
                    text = { Text(errorMessage ?: "", color = Color.White) },
                    containerColor = Obsidian,
                    textContentColor = Color.White
                )
            }

            // Movie Picker Bottom Sheet
            if (showMoviePicker) {
                MoviePickerBottomSheet(
                    onDismiss = { showMoviePicker = false },
                    onMovieSelected = { movie ->
                        selectedMovie = movie
                        showMoviePicker = false
                        showCreateDialog = true
                    },
                    viewModel = viewModel
                )
            }

            // Simplified Create Room Dialog
            if (showCreateDialog && selectedMovie != null) {
                CreateRoomDialog(
                    movie = selectedMovie!!,
                    onDismiss = { 
                        showCreateDialog = false 
                        selectedMovie = null
                    },
                    onCreate = { name, movieId, type, max ->
                        viewModel.createRoom(
                            CreateWatchRoomRequest(name, movieId, null, type, max),
                            onSuccess = { room ->
                                showCreateDialog = false
                                selectedMovie = null
                                onNavigateToRoom(room.id)
                            },
                            onFailure = { err ->
                                showCreateDialog = false
                                selectedMovie = null
                                errorMessage = err
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun WatchPartyHeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.15f), Obsidian)
                )
            )
            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Xem Phim Cùng Nhau",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tạo phòng xem chung, kết nối cùng bạn bè và thảo luận trực tiếp theo thời gian thực.",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun WatchPartyStepsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val steps = listOf(
            StepData(Icons.Default.Movie, "1. Chọn phim", "Chọn bộ phim muốn chiếu"),
            StepData(Icons.Default.Send, "2. Chia sẻ", "Gửi mã phòng cho bạn bè"),
            StepData(Icons.Default.PlayArrow, "3. Xem chung", "Đồng bộ phát & chat")
        )
        steps.forEach { step ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(step.icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(step.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(step.desc, color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
                }
            }
        }
    }
}

data class StepData(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val desc: String)

@Composable
fun WatchPartyEmptyState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có phòng nào hoạt động",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Hãy khởi xướng và tạo phòng chiếu phim đầu tiên!",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Text("✨ Tạo phòng chiếu", color = Obsidian, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePickerBottomSheet(
    onDismiss: () -> Unit,
    onMovieSelected: (MovieDto) -> Unit,
    viewModel: WatchPartyViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val pickerState by viewModel.moviePickerState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadPopularMoviesForPicker()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Obsidian,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Chọn phim để xem chung",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchMoviesForPicker(it)
                },
                placeholder = { Text("Tìm kiếm phim...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results UI state
            when (val state = pickerState) {
                is MoviePickerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
                is MoviePickerUiState.Success -> {
                    if (state.movies.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy phim phù hợp", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.movies) { movie ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onMovieSelected(movie) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(2f / 3f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                    ) {
                                        AsyncImage(
                                            model = movie.posterUrl ?: movie.thumbUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = movie.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                is MoviePickerUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    movie: MovieDto,
    onDismiss: () -> Unit,
    onCreate: (String, Long, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("PUBLIC") }
    var maxMembers by remember { mutableStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, movie.id, roomType, maxMembers)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                enabled = name.isNotBlank()
            ) {
                Text("Tạo", color = Obsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        },
        title = { Text("Tạo phòng Watch Party mới", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Movie preview card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp, 60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.DarkGray)
                    ) {
                        AsyncImage(
                            model = movie.posterUrl ?: movie.thumbUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Phim chiếu chung:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên phòng", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Loại phòng:", color = Color.White)
                    Row {
                        FilterChip(
                            selected = roomType == "PUBLIC",
                            onClick = { roomType = "PUBLIC" },
                            label = { Text("Công khai") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = roomType == "PRIVATE",
                            onClick = { roomType = "PRIVATE" },
                            label = { Text("Riêng tư") }
                        )
                    }
                }

                Column {
                    Text("Giới hạn số người: $maxMembers", color = Color.White)
                    Slider(
                        value = maxMembers.toFloat(),
                        onValueChange = { maxMembers = it.toInt() },
                        valueRange = 2f..50f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                }
            }
        },
        containerColor = Obsidian
    )
}

@Composable
fun PublicRoomCard(
    room: WatchRoomDto,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoinClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp, 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = room.movie.posterUrl ?: room.movie.thumbUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đang chiếu: ${room.movie.title}",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Members",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.currentMemberCount}/${room.maxMembers} thành viên",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Button(
                onClick = onJoinClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Vào", color = Obsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
