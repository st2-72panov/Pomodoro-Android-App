package com.example.pomodoroapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.R

class CompletionNotificationService(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun sendNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(null)
            .setContentTitle("ah-bruh cadda-bruh")
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun deleteNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "sound_channel"
        const val NOTIFICATION_ID = 1
    }
}