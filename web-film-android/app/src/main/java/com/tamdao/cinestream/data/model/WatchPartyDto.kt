package com.tamdao.cinestream.data.model

data class WatchRoomDto(
    val id: Long,
    val code: String,
    val name: String,
    val host: UserSummaryDto,
    val movie: MovieSummaryDto,
    val episode: EpisodeSummaryDto?,
    val roomType: String,
    val maxMembers: Int,
    val currentMemberCount: Int,
    val status: String,
    val createdAt: String
)

data class UserSummaryDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?
)

data class MovieSummaryDto(
    val id: Long,
    val title: String,
    val slug: String,
    val posterUrl: String?,
    val thumbUrl: String?
)

data class EpisodeSummaryDto(
    val id: Long,
    val name: String,
    val slug: String
)

data class CreateWatchRoomRequest(
    val name: String,
    val movieId: Long,
    val episodeId: Long?,
    val roomType: String,
    val maxMembers: Int
)

data class WatchRoomMemberDto(
    val id: Long,
    val roomId: Long,
    val userId: Long,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val role: String,
    val joinedAt: String,
    val leftAt: String?
)

data class VideoStateDto(
    val currentTime: Double,
    val isPlaying: Boolean,
    val playbackRate: Double,
    val lastSyncAt: String
)

data class ChatMessageDto(
    val id: Long,
    val userId: Long?,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val messageType: String,
    val timestamp: String
)

data class WatchPartyHistoryDto(
    val id: Long,
    val roomId: Long,
    val roomName: String,
    val roomCode: String,
    val movieId: Long,
    val movieTitle: String,
    val movieSlug: String,
    val moviePosterUrl: String?,
    val watchDurationSeconds: Int,
    val joinedAt: String,
    val leftAt: String
)
