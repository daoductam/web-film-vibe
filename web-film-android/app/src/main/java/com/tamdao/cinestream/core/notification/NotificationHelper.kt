package com.tamdao.cinestream.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tamdao.cinestream.MainActivity
import com.tamdao.cinestream.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NotificationHelper"
        private const val CHANNEL_ID = "new_episodes_channel"
        private const val CHANNEL_NAME = "Cập nhật tập mới"
        private const val CHANNEL_DESC = "Thông báo khi bộ phim bạn yêu thích có tập mới"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a rich notification with movie thumbnail and title.
     * Uses Coroutines to download the image via Coil asynchronously.
     */
    fun showNewEpisodeNotification(
        movieSlug: String,
        movieTitle: String,
        episodeName: String,
        thumbUrl: String?
    ) {
        val notificationId = movieSlug.hashCode()

        // Create Deep Link Intent
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("cinestream://movie/$movieSlug"),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(movieTitle)
            .setContentText("Đã cập nhật $episodeName")
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // fallback icon
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (thumbUrl.isNullOrEmpty()) {
            notificationManager.notify(notificationId, builder.build())
            return
        }

        // Load image using Coil asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = fetchBitmap(thumbUrl)
            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
                )
            }
            notificationManager.notify(notificationId, builder.build())
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Required to load image to system Notification
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading thumbnail: $url", e)
            null
        }
    }
}
