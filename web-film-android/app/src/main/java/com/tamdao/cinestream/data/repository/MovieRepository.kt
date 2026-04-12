package com.tamdao.cinestream.data.repository

import android.util.Log
import com.tamdao.cinestream.core.database.MovieDao
import com.tamdao.cinestream.core.database.MovieEntity
import com.tamdao.cinestream.core.database.WatchHistoryEntity
import com.tamdao.cinestream.core.database.FavoriteEntity
import com.tamdao.cinestream.core.network.MovieApiService
import com.tamdao.cinestream.data.model.ApiResponse
import com.tamdao.cinestream.data.model.MovieDetailDto
import com.tamdao.cinestream.data.model.MovieDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: MovieApiService,
    private val authApiService: com.tamdao.cinestream.core.network.AuthApiService,
    private val sessionManager: com.tamdao.cinestream.core.session.SessionManager,
    private val movieDao: MovieDao
) {
    private val TAG = "MovieRepository"

    fun getLatestMovies(): Flow<List<MovieDto>> = flow {
        // 1. Lấy dữ liệu từ Cache và phát ra ngay lập tức (nếu có)
        val initialCache = movieDao.getMoviesCache("LATEST").first()
        if (initialCache.isNotEmpty()) {
            emit(initialCache.map { it.toDto() })
        }

        // 2. Gọi API để lấy dữ liệu mới nhất
        try {
            val response = apiService.getLatestMovies(page = 0, size = 50)
            if (response.success && response.data != null) {
                val movies = response.data.content
                if (movies.isNotEmpty()) {
                    // Cập nhật Database
                    movieDao.clearCacheByType("LATEST")
                    movieDao.insertMovies(movies.map { it.toEntity("LATEST") })
                    
                    // Phát ra dữ liệu mới từ Network
                    emit(movies)
                }
            } else {
                throw Exception(response.message ?: "Lỗi API không xác định")
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                403 -> "Lỗi 403: Server từ chối truy cập (Kiểm tra Spring Security)"
                404 -> "Lỗi 404: Không tìm thấy API"
                else -> "Lỗi HTTP: ${e.code()}"
            }
            Log.e(TAG, errorMsg)
            throw Exception(errorMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Network Error: ${e.localizedMessage}")
            // Nếu cache rỗng và network lỗi thì mới ném lỗi
            if (initialCache.isEmpty()) throw e
        }
    }

    suspend fun getMovieDetail(slug: String): ApiResponse<MovieDetailDto> {
        return apiService.getMovieDetail(slug)
    }

    suspend fun getMoviesByType(type: String): List<MovieDto> {
        return try {
            val response = apiService.filterMovies(type = type, page = 0, size = 20)
            if (response.success && response.data != null) {
                response.data.content
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByFilter(
        query: String? = null,
        category: String? = null,
        year: Int? = null,
        page: Int = 0
    ): ApiResponse<com.tamdao.cinestream.data.model.PageResult<MovieDto>> {
        return if (query != null) {
            apiService.searchMovies(query, page)
        } else {
            apiService.filterMovies(category = category, year = year, page = page)
        }
    }

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = movieDao.getWatchHistory()

    suspend fun saveWatchHistory(history: WatchHistoryEntity) {
        movieDao.saveWatchHistory(history)
        
        // Sync to server if logged in
        if (sessionManager.isLoggedIn.first()) {
            try {
                authApiService.saveHistory(
                    com.tamdao.cinestream.core.network.WatchHistoryRequest(
                        movieSlug = history.slug,
                        title = history.title,
                        thumbUrl = history.thumbUrl,
                        lastEpisodeSlug = history.lastEpisodeSlug,
                        lastEpisodeName = history.lastEpisodeName,
                        progressMs = history.progressMs,
                        durationMs = history.durationMs
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync history to server: ${e.localizedMessage}")
            }
        }
    }

    // Favorites
    fun getFavorites(): Flow<List<MovieDto>> = movieDao.getAllFavorites().map { list ->
        list.map { it.toDto() }
    }

    fun isFavorite(slug: String): Flow<Boolean> = movieDao.isFavorite(slug)

    suspend fun toggleFavorite(movie: MovieDto) {
        val isFav = movieDao.isFavorite(movie.slug).first()
        if (isFav) {
            movieDao.deleteFavorite(movie.slug)
            
            // Sync remove to server if logged in
            if (sessionManager.isLoggedIn.first()) {
                try {
                    authApiService.removeFavorite(movie.slug)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove favorite from server: ${e.localizedMessage}")
                }
            }
        } else {
            movieDao.insertFavorite(movie.toFavoriteEntity())
            
            // Sync add to server if logged in
            if (sessionManager.isLoggedIn.first()) {
                try {
                    authApiService.addFavorite(
                        com.tamdao.cinestream.core.network.SyncFavoriteRequest(
                            movieSlug = movie.slug,
                            title = movie.title,
                            thumbUrl = movie.thumbUrl,
                            quality = movie.quality,
                            year = movie.year
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add favorite to server: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun MovieDto.toFavoriteEntity() = FavoriteEntity(
        slug = slug,
        title = title,
        thumbUrl = thumbUrl,
        quality = quality,
        year = year
    )

    suspend fun refreshFavorites() {
        if (sessionManager.isLoggedIn.first()) {
            try {
                val response = authApiService.getFavorites()
                if (response.success && response.data != null) {
                    val remoteFavs = response.data
                    // Update local DB
                    movieDao.clearAllFavorites()
                    remoteFavs.forEach { fav ->
                        movieDao.insertFavorite(
                            FavoriteEntity(
                                slug = fav.movieSlug,
                                title = fav.title,
                                thumbUrl = fav.thumbUrl ?: "",
                                quality = fav.quality,
                                year = fav.year ?: 0
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh favorites: ${e.localizedMessage}")
            }
        }
    }

    suspend fun refreshWatchHistory() {
        if (sessionManager.isLoggedIn.first()) {
            try {
                val response = authApiService.getHistory()
                if (response.success && response.data != null) {
                    val remoteHistory = response.data
                    remoteHistory.forEach { h ->
                        movieDao.saveWatchHistory(
                            WatchHistoryEntity(
                                slug = h.movieSlug,
                                title = h.title,
                                thumbUrl = h.thumbUrl ?: "",
                                lastEpisodeName = h.lastEpisodeName ?: "",
                                lastEpisodeSlug = h.lastEpisodeSlug ?: "",
                                progressMs = h.progressMs ?: 0L,
                                durationMs = h.durationMs ?: 0L,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh watch history: ${e.localizedMessage}")
            }
        }
    }

    private fun FavoriteEntity.toDto() = MovieDto(
        id = 0, // Id is not critical for UI cards
        title = title,
        originTitle = "",
        slug = slug,
        thumbUrl = thumbUrl,
        posterUrl = thumbUrl,
        year = year,
        type = "MOVIE",
        quality = quality,
        currentEpisode = null,
        language = null,
        viewCount = 0
    )

    private fun MovieDto.toEntity(type: String) = MovieEntity(
        id = id,
        title = title,
        originTitle = originTitle,
        slug = slug,
        thumbUrl = thumbUrl,
        posterUrl = posterUrl,
        year = year,
        type = type,
        quality = quality,
        cacheType = type
    )

    private fun MovieEntity.toDto() = MovieDto(
        id = id,
        title = title,
        originTitle = originTitle,
        slug = slug,
        thumbUrl = thumbUrl,
        posterUrl = posterUrl,
        year = year,
        type = type,
        quality = quality,
        currentEpisode = null,
        language = null,
        viewCount = 0
    )
}
