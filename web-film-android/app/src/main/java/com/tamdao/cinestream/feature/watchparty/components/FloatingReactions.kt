package com.tamdao.cinestream.feature.watchparty.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.tamdao.cinestream.feature.watchparty.WatchPartyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.random.Random

@Composable
fun FloatingReactions(
    viewModel: WatchPartyViewModel,
    modifier: Modifier = Modifier
) {
    val newReaction by viewModel.newReaction.collectAsState()
    val reactions = remember { mutableStateListOf<ReactionAnimation>() }

    // Listen for new incoming reactions to spawn animations
    LaunchedEffect(newReaction) {
        val react = newReaction ?: return@LaunchedEffect
        reactions.add(
            ReactionAnimation(
                emoji = react.emoji,
                startX = Random.nextFloat(), // Percent of screen width (0f to 1f)
                size = Random.nextInt(40, 80)
            )
        )
    }

    if (reactions.isNotEmpty()) {
        val transition = rememberInfiniteTransition()
        val time by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(16, easing = LinearEasing)
            )
        )

        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val iterator = reactions.iterator()
            while (iterator.hasNext()) {
                val react = iterator.next()
                
                // Update y progress, x offset wobble
                react.progress += 0.01f
                if (react.progress >= 1f) {
                    iterator.remove()
                    continue
                }

                val currentY = canvasHeight - (react.progress * canvasHeight)
                val drift = kotlin.math.sin(react.progress * 8f) * 50f
                val currentX = (react.startX * canvasWidth) + drift
                val alpha = ((1f - react.progress) * 255).toInt().coerceIn(0, 255)

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
}

class ReactionAnimation(
    val emoji: String,
    val startX: Float,
    val size: Int,
    var progress: Float = 0f
)
