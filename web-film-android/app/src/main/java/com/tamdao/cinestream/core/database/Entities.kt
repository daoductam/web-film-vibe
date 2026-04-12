package com.tamdao.cinestream.core.database

import androidx.room.*

@Entity(tableName = "movies_cache")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val originTitle: String?,
    val slug: String,
    val thumbUrl: String?,
    val posterUrl: String?,
    val year: Int?,
    val type: String?,
    val quality: String?,
    val cacheType: String // "LATEST", "POPULAR", etc.
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val thumbUrl: String,
    val lastEpisodeName: String,
    val lastEpisodeSlug: String,
    val progressMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val thumbUrl: String?,
    val quality: String?,
    val year: Int?,
    val createdAt: Long = System.currentTimeMillis()
)
