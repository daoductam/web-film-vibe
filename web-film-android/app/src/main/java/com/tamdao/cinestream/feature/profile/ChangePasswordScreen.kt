package com.tamdao.cinestream.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tamdao.cinestream.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val currentPassword by viewModel.currentPassword
    val newPassword by viewModel.newPassword
    val confirmPassword by viewModel.confirmPassword
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.success.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBar()
            )
        },
        containerColor = Obsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Current Password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { viewModel.onCurrentPasswordChange(it) },
                label = { Text("Mật khẩu hiện tại") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (currVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (currVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { currVisible = !currVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { viewModel.onNewPasswordChange(it) },
                label = { Text("Mật khẩu mới") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (newVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { newVisible = !newVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm New Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("Xác nhận mật khẩu mới") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confVisible = !confVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = error!!, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.changePassword() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Obsidian),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Obsidian)
                } else {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
