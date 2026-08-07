package com.tamdao.cinestream.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tamdao.cinestream.core.session.AuthViewModel
import com.tamdao.cinestream.core.session.SessionUser
import com.tamdao.cinestream.ui.theme.*

@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDownloadedMoviesClick: () -> Unit,
    onWatchPartyHistoryClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Text(
            text = "Cá nhân",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

        if (isLoggedIn && currentUser != null) {
            LoggedInContent(
                user = currentUser!!,
                onEditProfileClick = onEditProfileClick,
                onChangePasswordClick = onChangePasswordClick,
                onDownloadedMoviesClick = onDownloadedMoviesClick,
                onWatchPartyHistoryClick = onWatchPartyHistoryClick,
                onLogoutClick = { viewModel.logout() },
                viewModel = viewModel
            )
        } else {
            GuestContent(
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun LoggedInContent(
    user: SessionUser,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDownloadedMoviesClick: () -> Unit,
    onWatchPartyHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val scrollState = rememberScrollState()
    val syncState by viewModel.syncState.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when (currentThemeMode) {
        "LIGHT" -> "Sáng"
        "DARK" -> "Tối"
        else -> "Theo hệ thống"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        AsyncImage(
            model = user.avatarUrl ?: "https://www.gravatar.com/avatar/${user.id}?d=identicon",
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.fullName ?: user.username,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@${user.username}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Menu items
        ProfileMenuItem(
            icon = Icons.Default.Edit,
            label = "Chỉnh sửa hồ sơ",
            onClick = onEditProfileClick
        )
        ProfileMenuItem(
            icon = Icons.Default.Lock,
            label = "Đổi mật khẩu",
            onClick = onChangePasswordClick
        )
        ProfileMenuItem(
            icon = Icons.Default.Favorite,
            label = "Đồng bộ Favorites",
            subtitle = if (syncState is SyncStatus.Loading) "Đang đồng bộ..." else "Dữ liệu server làm gốc",
            onClick = { viewModel.syncFavorites() }
        )
        ProfileMenuItem(
            icon = Icons.Default.History,
            label = "Đồng bộ Lịch sử xem",
            onClick = { viewModel.syncHistory() }
        )
        ProfileMenuItem(
            icon = Icons.Default.Download,
            label = "Phim đã tải",
            onClick = onDownloadedMoviesClick
        )
        ProfileMenuItem(
            icon = Icons.Default.History,
            label = "Lịch sử Watch Party",
            onClick = onWatchPartyHistoryClick
        )
        
        ProfileMenuItem(
            icon = Icons.Default.Palette,
            label = "Giao diện",
            subtitle = themeLabel,
            onClick = { showThemeDialog = true }
        )

        if (syncState is SyncStatus.Success) {
            Text(
                text = (syncState as SyncStatus.Success).message,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.1f),
                contentColor = Color.Red
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Chọn giao diện") },
            text = {
                Column {
                    val options = listOf(
                        "SYSTEM" to "Theo hệ thống",
                        "LIGHT" to "Giao diện sáng",
                        "DARK" to "Giao diện tối"
                    )
                    options.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun GuestContent(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val currentThemeMode by viewModel.themeMode.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when (currentThemeMode) {
        "LIGHT" -> "Sáng"
        "DARK" -> "Tối"
        else -> "Theo hệ thống"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Đăng nhập để đồng bộ",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Lưu lại danh sách phim yêu thích và lịch sử xem của bạn trên mọi thiết bị.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Allow theme switching even for guests
        ProfileMenuItem(
            icon = Icons.Default.Palette,
            label = "Giao diện",
            subtitle = themeLabel,
            onClick = { showThemeDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng ký tài khoản", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = { /* Stay as guest */ }) {
            Text("Tiếp tục với tư cách khách", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Chọn giao diện") },
            text = {
                Column {
                    val options = listOf(
                        "SYSTEM" to "Theo hệ thống",
                        "LIGHT" to "Giao diện sáng",
                        "DARK" to "Giao diện tối"
                    )
                    options.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
