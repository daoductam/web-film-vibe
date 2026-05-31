package com.tamdao.cinestream.feature.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    movieSlug: String,
    episodeSlug: String,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Cấu hình xoay màn hình khi vào/ra trình phát
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(movieSlug, episodeSlug) {
        viewModel.loadEpisode(movieSlug, episodeSlug)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val state = uiState) {
            is PlayerUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonCyan)
            }
            is PlayerUiState.Success -> {
                // Kiểm tra xem có bản offline không
                val offlineEpisode by viewModel.offlineEpisode.collectAsState()
                val videoUrl = if (offlineEpisode?.downloadStatus == "COMPLETED" && !offlineEpisode?.localVideoPath.isNullOrEmpty()) {
                    offlineEpisode?.localVideoPath ?: ""
                } else {
                    state.episode.linkM3u8 ?: ""
                }

                ExoPlayerView(
                    url = videoUrl,
                    dataSourceFactory = viewModel.downloadManagerWrapper.createCacheDataSourceFactory(),
                    onBackClick = onBackClick,
                    onNextEpisode = {
                        val currentIndex = state.allEpisodes.indexOf(state.episode)
                        if (currentIndex != -1 && currentIndex < state.allEpisodes.size - 1) {
                            val nextEp = state.allEpisodes[currentIndex + 1]
                            viewModel.loadEpisode(state.movieSlug, nextEp.slug)
                        }
                    },
                    onDispose = { progress, duration ->
                        viewModel.saveProgress(
                            movieSlug = state.movieSlug,
                            movieTitle = state.movieTitle,
                            thumbUrl = state.thumbUrl,
                            episode = state.episode,
                            progressMs = progress,
                            durationMs = duration
                        )
                    }
                )
            }
            is PlayerUiState.Error -> {
                Text(text = state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    url: String,
    dataSourceFactory: androidx.media3.datasource.DataSource.Factory,
    onBackClick: () -> Unit,
    onNextEpisode: () -> Unit,
    onDispose: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
            )
            .build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            onNextEpisode()
                        }
                    }
                })
            }
    }

    LaunchedEffect(url) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            onDispose(exoPlayer.currentPosition, exoPlayer.duration)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Nút Quay lại
        var isInPipMode by remember { mutableStateOf(false) }
        val activity = context as? Activity
        
        DisposableEffect(context) {
            val listener = { mode: Boolean -> isInPipMode = mode }
            // Lưu ý: Trong thực tế bạn cần một cách để lắng nghe thay đổi PiP từ Activity
            onDispose {}
        }

        if (!isInPipMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    IconButton(onClick = {
                        val params = android.app.PictureInPictureParams.Builder().build()
                        activity?.enterPictureInPictureMode(params)
                    }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.PictureInPicture, 
                            contentDescription = "PiP", 
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
