package com.tamdao.cinestream.core.di

import android.content.Context
import com.tamdao.cinestream.core.download.DownloadManagerWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    @Provides
    @Singleton
    fun provideDownloadManagerWrapper(@ApplicationContext context: Context): DownloadManagerWrapper {
        return DownloadManagerWrapper(context)
    }
}
