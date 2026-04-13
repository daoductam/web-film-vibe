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
}
