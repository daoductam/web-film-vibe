package com.tamdao.cinestream.feature.detail.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tamdao.cinestream.data.model.CommentDto
import com.tamdao.cinestream.ui.theme.NeonCyan
import com.tamdao.cinestream.ui.theme.Obsidian

@Composable
fun StarRatingBar(
    rating: Double,
    count: Long,
    userRating: Int?,
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                val starValue = index + 1
                val isFilled = (userRating ?: 0) >= starValue || (userRating == null && rating >= starValue)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onRate(starValue) },
                    tint = if (isFilled) Color.Yellow else Color.Gray.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " ($count)",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        userRating?.let {
            Text(
                text = "Bạn đã đánh giá $it sao",
                color = NeonCyan,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun CommentSection(
    comments: List<CommentDto>,
    onLikeClick: (Long) -> Unit,
    onReplyClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onSendComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bình luận (${comments.size})",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Comment Input
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Viết bình luận...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedIndicatorColor = NeonCyan,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendComment(commentText)
                        commentText = ""
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Gửi", tint = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comments List
        comments.forEach { comment ->
            CommentItem(
                comment = comment,
                onLikeClick = onLikeClick,
                onReplyClick = onReplyClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentDto,
    onLikeClick: (Long) -> Unit,
    onReplyClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    isReply: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(start = if (isReply) 48.dp else 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(if (isReply) 24.dp else 32.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.take(1).uppercase(),
                    color = NeonCyan,
                    fontSize = if (isReply) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.fullName ?: comment.username,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = comment.content,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like
                    Row(
                        modifier = Modifier
                            .clickable { onLikeClick(comment.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (comment.isLiked) NeonCyan else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = comment.likeCount.toString(),
                            color = if (comment.isLiked) NeonCyan else Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Reply
                    if (!isReply) {
                        Text(
                            text = "Phản hồi",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onReplyClick(comment.id) }
                        )
                    }
                }
            }
        }

        // Recursive Replies
        comment.replies.forEach { reply ->
            CommentItem(
                comment = reply,
                onLikeClick = onLikeClick,
                onReplyClick = onReplyClick,
                onDeleteClick = onDeleteClick,
                isReply = true
            )
        }
    }
}
