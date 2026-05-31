package com.tamdao.cinestream.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Cache
    @Query("SELECT * FROM movies_cache WHERE cacheType = :type")
    fun getMoviesCache(type: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies_cache WHERE cacheType = :type")
    suspend fun clearCacheByType(type: String)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE slug = :slug")
    suspend fun deleteHistory(slug: String)

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun deleteFavorite(slug: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE slug = :slug)")
    fun isFavorite(slug: String): Flow<Boolean>

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    // Offline Mode
    @Query("SELECT * FROM offline_movies ORDER BY createdAt DESC")
    fun getAllOfflineMovies(): Flow<List<OfflineMovieEntity>>

    @Query("SELECT * FROM offline_movies WHERE slug = :slug")
    suspend fun getOfflineMovie(slug: String): OfflineMovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineMovie(movie: OfflineMovieEntity)

    @Query("DELETE FROM offline_movies WHERE slug = :slug")
    suspend fun deleteOfflineMovie(slug: String)

    // Offline Episodes Mode
    @Query("SELECT * FROM offline_episodes WHERE movieSlug = :movieSlug ORDER BY createdAt ASC")
    fun getOfflineEpisodesByMovie(movieSlug: String): Flow<List<OfflineEpisodeEntity>>

    @Query("SELECT * FROM offline_episodes ORDER BY createdAt DESC")
    fun getAllOfflineEpisodes(): Flow<List<OfflineEpisodeEntity>>

    @Query("SELECT * FROM offline_episodes")
    fun getOfflineEpisodesFlow(): Flow<List<OfflineEpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineEpisode(episode: OfflineEpisodeEntity)

    @Query("UPDATE offline_episodes SET downloadStatus = :status, localVideoPath = :path, progress = :progress WHERE episodeSlug = :episodeSlug")
    suspend fun updateEpisodeDownloadStatus(episodeSlug: String, status: String, path: String?, progress: Float)

    @Query("DELETE FROM offline_episodes WHERE episodeSlug = :episodeSlug")
    suspend fun deleteOfflineEpisode(episodeSlug: String)

    @Query("DELETE FROM offline_episodes WHERE movieSlug = :movieSlug")
    suspend fun deleteOfflineEpisodesByMovie(movieSlug: String)

    @Query("SELECT * FROM offline_episodes WHERE episodeSlug = :episodeSlug")
    suspend fun getOfflineEpisode(episodeSlug: String): OfflineEpisodeEntity?

}
