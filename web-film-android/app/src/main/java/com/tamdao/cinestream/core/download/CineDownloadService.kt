package com.tamdao.cinestream.core.download

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.tamdao.cinestream.R

@OptIn(UnstableApi::class)
class CineDownloadService : DownloadService(
    NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download_channel_name,
    0
) {

    override fun getDownloadManager(): DownloadManager {
        // Trong thực tế, bạn nên lấy instance singleton từ Hilt/Dagger
        // Ở đây ta giả định có một cách để truy cập DownloadManagerWrapper
        return (application as? com.tamdao.cinestream.CineStreamApp)?.downloadManagerWrapper?.downloadManager
            ?: throw IllegalStateException("DownloadManager not initialized")
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        // Tạo notification hiển thị tiến trình tải phim
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang tải phim...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "download_channel"
    }
}
