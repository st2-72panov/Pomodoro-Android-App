package com.example.pomodoroapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.R
import com.example.pomodoroapp.MainActivity

class CompletionNotificationService(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun sendNotification(timerType : String) {
        val activityIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityIntent)
            .setContentTitle("Timer $timerType finished")
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