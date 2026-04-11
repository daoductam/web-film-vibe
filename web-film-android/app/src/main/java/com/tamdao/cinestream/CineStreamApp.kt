package com.tamdao.cinestream

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CineStreamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Timber, Firebase, etc. here in the future
    }
}
