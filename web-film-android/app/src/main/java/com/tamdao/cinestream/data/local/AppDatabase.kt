package com.tamdao.cinestream.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tamdao.cinestream.data.local.dao.MovieDao
import com.tamdao.cinestream.data.local.entity.MovieEntity

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
