package com.tamdao.cinestream.core.download

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import kotlinx.coroutines.channels.awaitClose
import java.io.File
import java.util.concurrent.Executors

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DownloadManagerEntryPoint {
    fun movieDao(): com.tamdao.cinestream.core.database.MovieDao
}

@OptIn(UnstableApi::class)
class DownloadManagerWrapper(private val context: Context) {

    private val databaseProvider by lazy { StandaloneDatabaseProvider(context) }
    
    val downloadCache: SimpleCache
        get() = getDownloadCache(context)

    fun createCacheDataSourceFactory(): androidx.media3.datasource.cache.CacheDataSource.Factory {
        return androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheWriteDataSinkFactory(null)
            .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private val movieDao by lazy {
        EntryPointAccessors.fromApplication(context, DownloadManagerEntryPoint::class.java).movieDao()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    val downloadManager: DownloadManager by lazy {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            dataSourceFactory,
            Executors.newFixedThreadPool(3)
        ).apply {
            resumeDownloads()
            addListener(object : DownloadManager.Listener {
                override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                    val episodeSlug = if (download.request.data.isNotEmpty()) {
                        String(download.request.data)
                    } else {
                        download.request.id
                    }
                    android.util.Log.d(
                        "DownloadManagerWrapper", 
                        "Download State Changed: Slug=$episodeSlug, ID=${download.request.id}, State=${download.state}, FailureReason=${download.failureReason}, Progress=${download.percentDownloaded}%, Exception=${finalException?.message}"
                    )
                    scope.launch {
                        val status = when (download.state) {
                            Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> "DOWNLOADING"
                            Download.STATE_COMPLETED -> "COMPLETED"
                            Download.STATE_FAILED -> "FAILED"
                            Download.STATE_STOPPED -> "NONE"
                            else -> "NONE"
                        }
                        val path = if (download.state == Download.STATE_COMPLETED) {
                            download.request.uri.toString()
                        } else null
                        
                        val progress = download.percentDownloaded
                        movieDao.updateEpisodeDownloadStatus(episodeSlug, status, path, progress)
                    }
                }

                override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                    scope.launch {
                        val episodeSlug = if (download.request.data.isNotEmpty()) {
                            String(download.request.data)
                        } else {
                            download.request.id
                        }
                        movieDao.deleteOfflineEpisode(episodeSlug)
                    }
                }
            })
        }
    }

    fun startDownload(slug: String, url: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaId(slug)
            .build()
            
        val downloadHelper = DownloadHelper.forMediaItem(
            context,
            mediaItem,
            androidx.media3.exoplayer.DefaultRenderersFactory(context),
            DefaultHttpDataSource.Factory()
        )

        downloadHelper.prepare(object : DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                // Select default tracks (video, audio, etc.) for HLS downloading,
                // otherwise it only downloads the manifest file and completes instantly without saving media files.
                val parameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
                for (i in 0 until helper.periodCount) {
                    helper.addTrackSelection(i, parameters)
                }

                val downloadRequest = helper.getDownloadRequest(slug.toByteArray())
                android.util.Log.d("DownloadManagerWrapper", "DownloadHelper prepared. Starting download for $slug from $url")
                androidx.media3.exoplayer.offline.DownloadService.sendAddDownload(
                    context,
                    CineDownloadService::class.java,
                    downloadRequest,
                    /* foreground = */ true
                )
            }

            override fun onPrepareError(helper: DownloadHelper, e: java.io.IOException) {
                android.util.Log.e("DownloadManagerWrapper", "DownloadHelper prepare error for $slug: ${e.message}", e)
            }
        })
    }
    
    fun getDownload(slug: String): Download? {
        val downloads = downloadManager.downloadIndex.getDownloads()
        while (downloads.moveToNext()) {
            val download = downloads.download
            if (download.request.id == slug) {
                return download
            }
        }
        return null
    }

    fun cancelDownload(slug: String) {
        downloadManager.removeDownload(slug)
    }

    fun getAllDownloadsFlow(): kotlinx.coroutines.flow.Flow<List<Download>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = object : DownloadManager.Listener {
            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                val downloads = mutableListOf<Download>()
                val cursor = downloadManager.downloadIndex.getDownloads()
                while (cursor.moveToNext()) {
                    downloads.add(cursor.download)
                }
                trySend(downloads)
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                val downloads = mutableListOf<Download>()
                val cursor = downloadManager.downloadIndex.getDownloads()
                while (cursor.moveToNext()) {
                    downloads.add(cursor.download)
                }
                trySend(downloads)
            }
        }
        
        downloadManager.addListener(listener)
        
        // Initial emit
        val initialDownloads = mutableListOf<Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            initialDownloads.add(cursor.download)
        }
        trySend(initialDownloads)

        awaitClose {
            downloadManager.removeListener(listener)
        }
    }

    companion object {
        @Volatile
        private var cacheInstance: SimpleCache? = null

        @Synchronized
        fun getDownloadCache(context: Context): SimpleCache {
            if (cacheInstance == null) {
                val databaseProvider = StandaloneDatabaseProvider(context)
                val downloadDirectory = File(context.getExternalFilesDir(null), "downloads").apply {
                    if (!exists()) mkdirs()
                }
                cacheInstance = SimpleCache(downloadDirectory, NoOpCacheEvictor(), databaseProvider)
            }
            return cacheInstance!!
        }
    }
}
