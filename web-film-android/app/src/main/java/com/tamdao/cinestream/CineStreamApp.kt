package com.tamdao.cinestream

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import javax.inject.Inject

@HiltAndroidApp
class CineStreamApp : Application() {
    @Inject
    lateinit var downloadManagerWrapper: com.tamdao.cinestream.core.download.DownloadManagerWrapper

    override fun onCreate() {
        super.onCreate()
    }
}
