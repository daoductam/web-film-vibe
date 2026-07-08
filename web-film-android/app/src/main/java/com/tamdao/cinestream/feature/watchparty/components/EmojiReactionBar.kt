package com.tamdao.cinestream.feature.watchparty.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tamdao.cinestream.feature.watchparty.WatchPartyViewModel

@Composable
fun EmojiReactionBar(
    viewModel: WatchPartyViewModel,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("😍", "🔥", "😂", "👏", "❤️", "😱")

    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(CircleShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.sendReaction(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
    }
}
