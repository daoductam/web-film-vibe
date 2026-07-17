package com.tamdao.cinestream.core.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CineFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val TAG = "CineFCMService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            val type = remoteMessage.data["type"]
            if (type == "NEW_EPISODE") {
                val movieSlug = remoteMessage.data["movieSlug"]
                val movieTitle = remoteMessage.data["movieTitle"]
                val episodeName = remoteMessage.data["episodeName"]
                val thumbUrl = remoteMessage.data["thumbUrl"]

                if (movieSlug != null && movieTitle != null && episodeName != null) {
                    notificationHelper.showNewEpisodeNotification(
                        movieSlug = movieSlug,
                        movieTitle = movieTitle,
                        episodeName = episodeName,
                        thumbUrl = thumbUrl
                    )
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Note: With topic-based notifications, we do not need to register
        // the client token on the backend server.
    }
}
