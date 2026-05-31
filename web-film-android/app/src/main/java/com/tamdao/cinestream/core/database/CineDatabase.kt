package com.tamdao.cinestream.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MovieEntity::class, 
        WatchHistoryEntity::class, 
        FavoriteEntity::class, 
        OfflineMovieEntity::class,
        OfflineEpisodeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CineDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
