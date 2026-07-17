package com.tamdao.cinestream

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

import javax.inject.Inject

@HiltAndroidApp
class CineStreamApp : Application() {
    @Inject
    lateinit var downloadManagerWrapper: com.tamdao.cinestream.core.download.DownloadManagerWrapper

    @Inject
    lateinit var notificationHelper: com.tamdao.cinestream.core.notification.NotificationHelper

    override fun onCreate() {
        super.onCreate()
        // Kích hoạt khởi tạo để NotificationHelper tạo channel ngay lập tức
        Log.d("CineStreamApp", "Initializing notification channels")
    }
}
