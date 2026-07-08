package com.tamdao.cinestream.feature.watchparty.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tamdao.cinestream.feature.watchparty.WatchPartyViewModel
import com.tamdao.cinestream.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CountdownOverlay(
    viewModel: WatchPartyViewModel,
    modifier: Modifier = Modifier
) {
    val presenceEvent by viewModel.presenceEvent.collectAsState()
    var countdownValue by remember { mutableStateOf(-1) }

    // Listen for Countdown system presence trigger
    // Since we'll broadcast a COUNTDOWN event in Phase 2 systems
    LaunchedEffect(presenceEvent) {
        val event = presenceEvent ?: return@LaunchedEffect
        if (event.type == "COUNTDOWN") {
            countdownValue = 5
        }
    }

    LaunchedEffect(countdownValue) {
        if (countdownValue > 0) {
            delay(1000)
            countdownValue -= 1
        }
    }

    if (countdownValue >= 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = countdownValue,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.5f) with
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.5f)
                }
            ) { value ->
                Text(
                    text = if (value == 0) "XEM PHIM! 🎬" else value.toString(),
                    color = NeonCyan,
                    fontSize = if (value == 0) 48.sp else 90.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
