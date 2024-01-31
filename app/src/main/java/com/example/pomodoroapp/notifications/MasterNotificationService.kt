package com.example.pomodoroapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.pages.MainActivity
import com.example.pomodoroapp.R

class MasterNotificationService(
    private val context: Context
) {
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification() {
        val activityIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val closeNotificationIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, CloseMasterNotificationReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pomodoro Timer")
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(activityIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,  // is not used
                "Close",
                closeNotificationIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    fun deleteNotification() { notificationManager.cancel(NOTIFICATION_ID) }

    companion object {
        const val CHANNEL_ID = "master_channel"
        const val NOTIFICATION_ID = 1
    }
}