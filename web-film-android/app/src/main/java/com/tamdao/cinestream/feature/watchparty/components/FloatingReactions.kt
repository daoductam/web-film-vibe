package com.tamdao.cinestream.feature.watchparty.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.tamdao.cinestream.feature.watchparty.WatchPartyViewModel
import kotlinx.coroutines.isActive
import kotlin.random.Random

// Total flight duration for each emoji particle (milliseconds)
private const val REACTION_DURATION_MS = 1200L

// Stable data holder: progress is a Compose State so Canvas recomposes on change
class ReactionAnimation(
    val emoji: String,
    val startX: Float,
    val size: Int
) {
    var progress by mutableStateOf(0f)
}

@Composable
fun FloatingReactions(
    viewModel: WatchPartyViewModel,
    modifier: Modifier = Modifier
) {
    val newReaction by viewModel.newReaction.collectAsState()
    val reactions = remember { mutableStateListOf<ReactionAnimation>() }

    // Spawn a new particle whenever a reaction arrives
    LaunchedEffect(newReaction) {
        val react = newReaction ?: return@LaunchedEffect
        reactions.add(
            ReactionAnimation(
                emoji = react.emoji,
                startX = Random.nextFloat(),
                size = Random.nextInt(40, 80)
            )
        )
    }

    // Game-loop coroutine synced to Vsync via withInfiniteAnimationFrameMillis.
    // Delta-time ensures emoji always completes flight in exactly REACTION_DURATION_MS,
    // regardless of device frame rate or Compose recompose frequency.
    LaunchedEffect(Unit) {
        var lastFrameMs = withInfiniteAnimationFrameMillis { it }
        while (isActive) {
            val frameMs = withInfiniteAnimationFrameMillis { it }
            val deltaMs = (frameMs - lastFrameMs).coerceAtMost(64L)
            lastFrameMs = frameMs

            if (reactions.isNotEmpty()) {
                val deltaProgress = deltaMs.toFloat() / REACTION_DURATION_MS
                val toRemove = mutableListOf<ReactionAnimation>()
                for (react in reactions) {
                    react.progress += deltaProgress
                    if (react.progress >= 1f) toRemove.add(react)
                }
                reactions.removeAll(toRemove)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (react in reactions) {
            val progress = react.progress
            val currentY = canvasHeight - (progress * canvasHeight)
            val drift = kotlin.math.sin(progress * 8f) * 50f
            val currentX = (react.startX * canvasWidth) + drift
            val alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize = react.size.toFloat()
                    this.alpha = alpha
                }
                canvas.nativeCanvas.drawText(
                    react.emoji,
                    currentX,
                    currentY,
                    paint
                )
            }
        }
    }
}
