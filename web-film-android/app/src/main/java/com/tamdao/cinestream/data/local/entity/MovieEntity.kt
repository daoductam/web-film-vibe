package com.tamdao.cinestream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val slug: String,
    val id: Long,
    val title: String,
    val originTitle: String?,
    val thumbUrl: String?,
    val posterUrl: String?,
    val year: Int?,
    val description: String?,
    val type: String?,
    val quality: String?,
    val language: String?,
    val director: String?,
    val actors: String?,
    val categories: String, // Stored as JSON or comma-separated
    val servers: String,    // Stored as JSON
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
