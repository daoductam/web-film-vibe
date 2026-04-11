package com.tamdao.cinestream.data.model

data class MovieDto(
    val id: Long,
    val title: String,
    val originTitle: String,
    val slug: String,
    val thumbUrl: String,
    val posterUrl: String,
    val year: Int,
    val type: String,
    val currentEpisode: String?,
    val quality: String?,
    val language: String?,
    val viewCount: Long
)

data class MovieDetailDto(
    val id: Long,
    val title: String,
    val originTitle: String,
    val slug: String,
    val thumbUrl: String,
    val posterUrl: String,
    val year: Int,
    val description: String?,
    val status: String?,
    val type: String,
    val viewCount: Long,
    val totalEpisodes: Int?,
    val currentEpisode: String?,
    val quality: String?,
    val language: String?,
    val duration: String?,
    val director: String?,
    val actors: String?,
    val categories: List<CategoryDto>,
    val countries: List<CountryDto>,
    val servers: List<ServerEpisodeGroupDto>
)

data class CategoryDto(val id: Long?, val name: String, val slug: String)
data class CountryDto(val id: Long?, val name: String, val slug: String)

data class ServerEpisodeGroupDto(
    val serverName: String,
    val episodes: List<EpisodeDto>
)

data class EpisodeDto(
    val serverName: String?,
    val name: String,
    val slug: String,
    val linkEmbed: String?,
    val linkM3u8: String?
)
