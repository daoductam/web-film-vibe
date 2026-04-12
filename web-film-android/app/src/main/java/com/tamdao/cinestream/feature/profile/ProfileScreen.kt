package com.tamdao.cinestream.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
    ) {
        // Header
        Text(
            text = "Cá nhân",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp)
        )

        Divider(color = Color.White.copy(alpha = 0.1f))

        if (isLoggedIn && currentUser != null) {
            LoggedInContent(
                user = currentUser!!,
                onEditProfileClick = onEditProfileClick,
                onChangePasswordClick = onChangePasswordClick,
                onLogoutClick = { viewModel.logout() }
            )
        } else {
            GuestContent(
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick
            )
        }
    }
}

@Composable
fun LoggedInContent(
    user: SessionUser,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                .background(SurfaceDark),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.fullName ?: user.username,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@${user.username}",
            color = NeonCyan,
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
            subtitle = "Dữ liệu server làm gốc",
            onClick = { /* Handle sync trigger */ }
        )
        ProfileMenuItem(
            icon = Icons.Default.History,
            label = "Đồng bộ Lịch sử xem",
            onClick = { /* Handle sync trigger */ }
        )

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
}

@Composable
fun GuestContent(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
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
            tint = NeonCyan,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Đăng nhập để đồng bộ",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Lưu lại danh sách phim yêu thích và lịch sử xem của bạn trên mọi thiết bị.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Obsidian),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng ký tài khoản", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = { /* Stay as guest */ }) {
            Text("Tiếp tục với tư cách khách", color = Color.Gray)
        }
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
            color = SurfaceDark,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
