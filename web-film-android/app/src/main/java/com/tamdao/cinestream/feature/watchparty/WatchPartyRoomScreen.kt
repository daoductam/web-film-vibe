package com.tamdao.cinestream.feature.watchparty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tamdao.cinestream.data.model.ChatMessageDto
import com.tamdao.cinestream.data.model.WatchRoomDto
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchPartyRoomScreen(
    roomId: Long,
    onLeaveClick: () -> Unit,
    viewModel: WatchPartyViewModel = hiltViewModel()
) {
    val roomState by viewModel.roomState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var chatInput by remember { mutableStateOf("") }
    var isSpoilerMessage by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        viewModel.enterRoom(roomId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveRoom()
        }
    }

    when (val state = roomState) {
        is WatchPartyRoomUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Obsidian),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        }
        is WatchPartyRoomUiState.Success -> {
            val room = state.room
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(room.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text("Đang xem: ${room.movie.title}", color = Color.Gray, fontSize = 12.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onLeaveClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Rời phòng", tint = Color.White)
                            }
                        },
                        actions = {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            IconButton(
                                onClick = {
                                    val activity = context as? android.app.Activity
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        val params = android.app.PictureInPictureParams.Builder().build()
                                        activity?.enterPictureInPictureMode(params)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "Thu nhỏ PiP",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Thông tin phòng", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
                    )
                },
                containerColor = Obsidian
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Video Player
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val isHost = room.host.username == viewModel.getCurrentUsername() // checks identity
                        
                        com.tamdao.cinestream.feature.watchparty.components.SyncVideoPlayer(
                            url = state.videoUrl, // Stream source URL (HLS / MP4)
                            isHost = isHost,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Emoji Reactions layer
                        com.tamdao.cinestream.feature.watchparty.components.FloatingReactions(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Countdown overlay layer
                        com.tamdao.cinestream.feature.watchparty.components.CountdownOverlay(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Emoji reaction quick selector
                    com.tamdao.cinestream.feature.watchparty.components.EmojiReactionBar(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Chat messages list
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Obsidian)
                    ) {
                        val listState = rememberLazyListState()
                        
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { msg ->
                                ChatBubble(message = msg)
                            }
                        }
                    }

                    // Chat input bar
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSpoilerMessage,
                                    onCheckedChange = { isSpoilerMessage = it },
                                    colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                                )
                                Text("Gửi dưới dạng Spoiler (Mờ nội dung)", color = Color.LightGray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Nhập tin nhắn... (Gõ @AI để gọi bot)", color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            viewModel.sendMessage(
                                                content = chatInput,
                                                messageType = if (isSpoilerMessage) "SPOILER" else "CHAT"
                                            )
                                            chatInput = ""
                                            isSpoilerMessage = false
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Gửi",
                                        tint = Obsidian
                                    )
                                }
                            }
                        }
                    }
                }

                // Info Dialog
                if (showInfoDialog) {
                    AlertDialog(
                        onDismissRequest = { showInfoDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showInfoDialog = false }) {
                                Text("Đóng", color = NeonCyan)
                            }
                        },
                        title = { Text("Thông tin phòng", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Tên phòng: ${room.name}", color = Color.White)
                                Text("Đang xem: ${room.movie.title}", color = Color.White)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mã phòng: ${room.code}", color = NeonCyan, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(room.code))
                                    }) {
                                        Text("Sao chép", color = NeonCyan)
                                    }
                                }
                                Text("Host: ${room.host.fullName ?: room.host.username}", color = Color.White)
                                Text("Loại phòng: ${if (room.roomType == "PUBLIC") "Công khai" else "Riêng tư"}", color = Color.White)
                                Text("Tối đa thành viên: ${room.maxMembers}", color = Color.White)
                            }
                        },
                        containerColor = Obsidian,
                        textContentColor = Color.White
                    )
                }
            }
        }
        is WatchPartyRoomUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Obsidian),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLeaveClick, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                        Text("Quay lại", color = Obsidian)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageDto) {
    val isBot = message.userId == null && "AI_RESPONSE" == message.messageType
    val isSystem = "SYSTEM" == message.messageType
    var isSpoilerRevealed by remember { mutableStateOf(false) }

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.content,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
    ) {
        if (isBot) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isBot) Alignment.Start else Alignment.End
        ) {
            Text(
                text = message.username,
                color = if (isBot) NeonCyan else Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isBot) Color(0xFF006064) else Color.White.copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if ("SPOILER" == message.messageType && !isSpoilerRevealed) {
                    Row(
                        modifier = Modifier.clickable { isSpoilerRevealed = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nội dung spoiler. Bấm để xem!",
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
