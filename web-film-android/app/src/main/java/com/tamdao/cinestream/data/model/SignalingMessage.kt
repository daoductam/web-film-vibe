package com.tamdao.cinestream.data.model

data class SignalingMessage(
    val type: String,
    val targetUserId: Long,
    val payload: String
)
