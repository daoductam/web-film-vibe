package com.tamdao.cinestream.feature.watchparty.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamdao.cinestream.feature.watchparty.WatchPartyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun SyncVideoPlayer(
    url: String,
    isHost: Boolean,
    viewModel: WatchPartyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val syncCommand by viewModel.syncCommand.collectAsState()
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false // Wait for host sync
        }
    }

    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            val mediaItem = androidx.media3.common.MediaItem.Builder()
                .setUri(url)
                .apply {
                    if (url.contains(".m3u8", ignoreCase = true)) {
                        setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    }
                }
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    var ignoreNextLocalStateChange by remember { mutableStateOf(false) }

    // ExoPlayer Listener to send changes if User is HOST/CO_HOST
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isHost) return
                if (ignoreNextLocalStateChange) {
                    ignoreNextLocalStateChange = false
                    return
                }

                val action = if (isPlaying) "PLAY" else "PAUSE"
                val currentSecs = exoPlayer.currentPosition / 1000.0
                viewModel.sendSyncCommand(action, currentSecs)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (!isHost) return
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    if (ignoreNextLocalStateChange) {
                        ignoreNextLocalStateChange = false
                        return
                    }
                    val currentSecs = newPosition.positionMs / 1000.0
                    viewModel.sendSyncCommand("SEEK", currentSecs)
                }
            }
        }

        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // React to remote Stomp Sync Commands (MEMBER side syncs)
    LaunchedEffect(syncCommand) {
        val cmd = syncCommand ?: return@LaunchedEffect
        val targetMs = (cmd.timestamp * 1000).toLong()
        val localMs = exoPlayer.currentPosition
        val drift = abs(localMs - targetMs)

        if (!isHost) {
            // Apply drift sync to members
            when (cmd.action) {
                "PLAY" -> {
                    ignoreNextLocalStateChange = true
                    if (drift > 2000) exoPlayer.seekTo(targetMs)
                    exoPlayer.playWhenReady = true
                }
                "PAUSE" -> {
                    ignoreNextLocalStateChange = true
                    exoPlayer.playWhenReady = false
                }
                "SEEK" -> {
                    ignoreNextLocalStateChange = true
                    exoPlayer.seekTo(targetMs)
                }
                "HEARTBEAT" -> {
                    // Drift correction
                    if (drift > 2000) {
                        ignoreNextLocalStateChange = true
                        exoPlayer.seekTo(targetMs)
                    }
                }
            }
        }
    }

    // HOST side Heartbeats (every 5 seconds)
    if (isHost) {
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                if (exoPlayer.isPlaying) {
                    val currentSecs = exoPlayer.currentPosition / 1000.0
                    viewModel.sendSyncCommand("HEARTBEAT", currentSecs)
                }
            }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = isHost // Only host can click control bar
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
