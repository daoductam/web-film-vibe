package com.tamdao.cinestream.data.repository

import android.util.Log
import com.tamdao.cinestream.core.database.MovieDao
import com.tamdao.cinestream.core.database.MovieEntity
import com.tamdao.cinestream.core.database.WatchHistoryEntity
import com.tamdao.cinestream.core.database.FavoriteEntity
import com.tamdao.cinestream.core.network.MovieApiService
import com.tamdao.cinestream.data.model.ApiResponse
import com.tamdao.cinestream.data.model.CategoryDto
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
            Log.e(TAG, "HTTP Error: ${e.code()} - ${e.message()}")
            throw e
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

    suspend fun getCategories(): List<CategoryDto> {
        return try {
            val response = apiService.getCategories()
            if (response.success && response.data != null) {
                response.data
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByFilter(
        query: String? = null,
        type: String? = null,
        categories: List<String>? = null,
        year: Int? = null,
        page: Int = 0
    ): ApiResponse<com.tamdao.cinestream.data.model.PageResult<MovieDto>> {
        return if (query != null) {
            apiService.searchMovies(query, page)
        } else if (type == "latest") {
            apiService.getLatestMovies(page = page)
        } else {
            apiService.filterMovies(type = type, categories = categories, year = year, page = page)
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

    // Offline Mode
    fun getAllOfflineMovieEntities(): Flow<List<com.tamdao.cinestream.core.database.OfflineMovieEntity>> {
        return movieDao.getAllOfflineMovies()
    }

    fun getAllOfflineMovies(): Flow<List<MovieDto>> = movieDao.getAllOfflineMovies().map { list ->
        list.map { it.toDto() }
    }

    suspend fun getOfflineMovie(slug: String): com.tamdao.cinestream.core.database.OfflineMovieEntity? {
        return movieDao.getOfflineMovie(slug)
    }

    suspend fun getOfflineEpisode(episodeSlug: String): com.tamdao.cinestream.core.database.OfflineEpisodeEntity? {
        return movieDao.getOfflineEpisode(episodeSlug)
    }

    suspend fun saveMovieOffline(movie: MovieDetailDto) {
        val gson = com.google.gson.Gson()
        val entity = com.tamdao.cinestream.core.database.OfflineMovieEntity(
            slug = movie.slug,
            id = movie.id,
            title = movie.title,
            thumbUrl = movie.thumbUrl,
            posterUrl = movie.posterUrl,
            description = movie.description,
            quality = movie.quality,
            duration = movie.duration,
            director = movie.director,
            actors = movie.actors,
            serversJson = gson.toJson(movie.servers)
        )
        movieDao.insertOfflineMovie(entity)
    }

    suspend fun getOfflineMovieDetail(slug: String): MovieDetailDto? {
        val entity = movieDao.getOfflineMovie(slug) ?: return null
        val gson = com.google.gson.Gson()
        val typeToken = object : com.google.gson.reflect.TypeToken<List<com.tamdao.cinestream.data.model.ServerEpisodeGroupDto>>() {}.type
        val servers: List<com.tamdao.cinestream.data.model.ServerEpisodeGroupDto> = try {
            gson.fromJson(entity.serversJson, typeToken)
        } catch (e: Exception) {
            emptyList()
        }
        return MovieDetailDto(
            id = entity.id,
            title = entity.title,
            originTitle = entity.title,
            slug = entity.slug,
            thumbUrl = entity.thumbUrl ?: "",
            posterUrl = entity.posterUrl ?: "",
            year = 0,
            description = entity.description,
            status = null,
            type = "OFFLINE",
            viewCount = 0,
            totalEpisodes = servers.flatMap { it.episodes }.size,
            currentEpisode = null,
            quality = entity.quality,
            language = null,
            duration = entity.duration,
            director = entity.director,
            actors = entity.actors,
            categories = emptyList(),
            countries = emptyList(),
            servers = servers,
            averageRating = null,
            ratingCount = null
        )
    }


    fun getOfflineEpisodesByMovie(movieSlug: String): Flow<List<com.tamdao.cinestream.core.database.OfflineEpisodeEntity>> {
        return movieDao.getOfflineEpisodesByMovie(movieSlug)
    }

    fun getAllOfflineEpisodes(): Flow<List<com.tamdao.cinestream.core.database.OfflineEpisodeEntity>> {
        return movieDao.getAllOfflineEpisodes()
    }

    fun getOfflineEpisodesFlow(): Flow<List<com.tamdao.cinestream.core.database.OfflineEpisodeEntity>> {
        return movieDao.getOfflineEpisodesFlow()
    }

    suspend fun saveEpisodeOffline(episodeSlug: String, movieSlug: String, episodeName: String, videoUrl: String) {
        val entity = com.tamdao.cinestream.core.database.OfflineEpisodeEntity(
            episodeSlug = episodeSlug,
            movieSlug = movieSlug,
            episodeName = episodeName,
            videoUrl = videoUrl,
            downloadStatus = "DOWNLOADING",
            progress = 0f
        )
        movieDao.insertOfflineEpisode(entity)
    }

    suspend fun updateEpisodeDownloadStatus(episodeSlug: String, status: String, path: String?, progress: Float) {
        movieDao.updateEpisodeDownloadStatus(episodeSlug, status, path, progress)
    }

    suspend fun deleteOfflineEpisode(episodeSlug: String) {
        movieDao.deleteOfflineEpisode(episodeSlug)
    }

    suspend fun deleteOfflineMovieAndEpisodes(movieSlug: String) {
        movieDao.deleteOfflineMovie(movieSlug)
        movieDao.deleteOfflineEpisodesByMovie(movieSlug)
    }


    private fun com.tamdao.cinestream.core.database.OfflineMovieEntity.toDto() = MovieDto(
        id = id,
        title = title,
        slug = slug,
        thumbUrl = thumbUrl,
        posterUrl = posterUrl,
        quality = quality,
        year = 0, // Not stored in offline entity for now
        type = "OFFLINE"
    )

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

    fun getPersonalizedRecommendations(): Flow<List<MovieDto>> = flow {
        if (sessionManager.isLoggedIn.first()) {
            try {
                val response = authApiService.getPersonalizedRecommendations()
                val list = response.data.personalizedRecommendations.map { it.toMovieDto() }
                emit(list)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Failed to get personalized recommendations: ${e.localizedMessage}")
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }

    fun getSimilarMovies(slug: String): Flow<List<MovieDto>> = flow {
        try {
            val response = authApiService.getSimilarMovies(
                com.tamdao.cinestream.core.network.GraphQLRequest(
                    query = "query(\$slug: String!) { similarMovies(slug: \$slug) { id title slug posterUrl views rating } }",
                    variables = mapOf("slug" to slug)
                )
            )
            val list = response.data.similarMovies.map { it.toMovieDto() }
            emit(list)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Failed to get similar movies: ${e.localizedMessage}")
            emit(emptyList())
        }
    }

    private fun com.tamdao.cinestream.core.network.MovieNodeDto.toMovieDto() = MovieDto(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        originTitle = "",
        slug = slug,
        thumbUrl = posterUrl ?: "",
        posterUrl = posterUrl ?: "",
        year = 0,
        type = "MOVIE",
        quality = "HD",
        currentEpisode = null,
        language = null,
        viewCount = (views ?: 0).toLong()
    )
}
