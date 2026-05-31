package com.tamdao.cinestream.data.local.dao

import androidx.room.*
import com.tamdao.cinestream.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE slug = :slug")
    suspend fun getMovieBySlug(slug: String): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("UPDATE movies SET isDownloaded = :isDownloaded, downloadPath = :path WHERE slug = :slug")
    suspend fun updateDownloadStatus(slug: String, isDownloaded: Boolean, path: String?)
}
